/**
 * Google Cloud Billing Circuit Breaker
 * Subscribes to Pub/Sub topic 'billing-budget-alerts'
 * Automatically halts billable resources if monthly spending reaches 100% of budget.
 *
 * Deployment (Google Cloud CLI):
 * gcloud functions deploy stopBillingOnBudgetCap \
 *   --runtime nodejs18 \
 *   --trigger-topic billing-budget-alerts \
 *   --entry-point stopBillingOnBudgetCap
 */

const { CloudBillingClient } = require('@google-cloud/billing');
const billing = new CloudBillingClient();

const PROJECT_ID = process.env.GOOGLE_CLOUD_PROJECT || 'engraceddispatch-ffba4';
const PROJECT_NAME = `projects/${PROJECT_ID}`;

/**
 * Determine whether the budget notification indicates that budget has been exceeded.
 * @param {object} pubsubData - Decoded Pub/Sub message data.
 * @returns {boolean}
 */
function isBudgetExceeded(pubsubData) {
  const costAmount = pubsubData.costAmount || 0;
  const budgetAmount = pubsubData.budgetAmount || 0;

  console.log(`[Billing Alert] Current cost: ${costAmount}, Budget limit: ${budgetAmount}`);
  return budgetAmount > 0 && costAmount >= budgetAmount;
}

/**
 * Disables billing for the project.
 */
async function disableBillingForProject() {
  try {
    const [billingInfo] = await billing.getProjectBillingInfo({ name: PROJECT_NAME });
    if (!billingInfo.billingEnabled) {
      console.log(`[Billing Circuit Breaker] Billing is already disabled for ${PROJECT_NAME}.`);
      return;
    }

    console.warn(`[CIRCUIT BREAKER TRIGGERED] Disabling billing for project ${PROJECT_NAME} to prevent runaway charges.`);
    await billing.updateProjectBillingInfo({
      name: PROJECT_NAME,
      projectBillingInfo: {
        billingAccountName: '' // Empty string detaches billing account
      }
    });
    console.log(`[SUCCESS] Billing account detached successfully from ${PROJECT_NAME}.`);
  } catch (err) {
    console.error(`[ERROR] Failed to disable billing:`, err);
  }
}

/**
 * Entry point for Pub/Sub notification
 */
exports.stopBillingOnBudgetCap = async (pubsubEvent, context) => {
  try {
    const rawData = pubsubEvent.data
      ? Buffer.from(pubsubEvent.data, 'base64').toString()
      : '{}';
    const budgetData = JSON.parse(rawData);

    if (isBudgetExceeded(budgetData)) {
      await disableBillingForProject();
    } else {
      console.log(`[OK] Budget within safe limits. No action taken.`);
    }
  } catch (err) {
    console.error(`[ERROR] Processing billing alert failed:`, err);
  }
};
