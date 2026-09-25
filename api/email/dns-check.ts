import dns from "dns";

export interface DnsCheckResult {
  domain: string;
  spf: {
    exists: boolean;
    record?: string;
    isAuthorizedForHost: boolean;
    expectedRecord: string;
    status: "valid" | "warning" | "missing";
    message: string;
  };
  dmarc: {
    exists: boolean;
    record?: string;
    expectedRecord: string;
    status: "valid" | "missing";
    message: string;
  };
  mx: {
    exists: boolean;
    records: Array<{ exchange: string; priority: number }>;
    expectedRecord: string;
    status: "valid" | "missing";
    message: string;
  };
  summary: {
    isInboxReady: boolean;
    spamRiskLevel: "CRITICAL" | "MODERATE" | "LOW";
    recommendations: string[];
  };
}

export default async function handler(req: any, res: any) {
  const queryDomain = (req.query?.domain || req.body?.domain || "engracedsmile.com").trim().toLowerCase();

  try {
    const { resolveTxt, resolveMx } = dns.promises;

    // 1. Resolve TXT records on root domain (SPF)
    let rootTxtRecords: string[][] = [];
    try {
      rootTxtRecords = await resolveTxt(queryDomain);
    } catch (e: any) {
      // ENODATA or ENOTFOUND is normal when no TXT exists
    }

    const flatTxt = rootTxtRecords.map((chunks) => chunks.join(""));
    const spfRecord = flatTxt.find((txt) => txt.toLowerCase().startsWith("v=spf1"));
    const expectedSpf = "v=spf1 ip4:5.39.69.62 include:server.hostnextdns.com ~all";

    const spfExists = Boolean(spfRecord);
    const spfIncludesHost = spfExists && (spfRecord!.includes("5.39.69.62") || spfRecord!.includes("server.hostnextdns.com"));

    // 2. Resolve DMARC records on _dmarc.domain
    let dmarcTxtRecords: string[][] = [];
    try {
      dmarcTxtRecords = await resolveTxt(`_dmarc.${queryDomain}`);
    } catch (e: any) {
      // ENODATA or ENOTFOUND
    }

    const flatDmarc = dmarcTxtRecords.map((chunks) => chunks.join(""));
    const dmarcRecord = flatDmarc.find((txt) => txt.toUpperCase().startsWith("V=DMARC1") || txt.toLowerCase().startsWith("v=dmarc1"));
    const expectedDmarc = `v=DMARC1; p=none; rua=mailto:support@${queryDomain}; aspf=r;`;
    const dmarcExists = Boolean(dmarcRecord);

    // 3. Resolve MX records
    let mxRecords: Array<{ exchange: string; priority: number }> = [];
    try {
      mxRecords = await resolveMx(queryDomain);
    } catch (e: any) {
      // ENODATA or ENOTFOUND
    }
    const mxExists = mxRecords && mxRecords.length > 0;
    const expectedMx = "server.hostnextdns.com (Priority 10)";

    // Determine recommendations and risk
    const recommendations: string[] = [];
    let spamRiskLevel: "CRITICAL" | "MODERATE" | "LOW" = "LOW";

    if (!spfExists) {
      spamRiskLevel = "CRITICAL";
      recommendations.push(
        `Add SPF TXT record: Name '@', Value '${expectedSpf}'. Gmail & Yahoo automatically flag emails as SPAM without SPF.`
      );
    } else if (!spfIncludesHost) {
      if (spamRiskLevel !== "CRITICAL") spamRiskLevel = "MODERATE";
      recommendations.push(
        `Update SPF TXT record to authorize HostNextDNS IP (5.39.69.62) or include:server.hostnextdns.com.`
      );
    }

    if (!dmarcExists) {
      if (spamRiskLevel !== "CRITICAL") spamRiskLevel = "MODERATE";
      recommendations.push(
        `Add DMARC TXT record: Name '_dmarc', Value '${expectedDmarc}'. Required by 2024 Google/Yahoo inbox standards.`
      );
    }

    if (!mxExists) {
      recommendations.push(
        `Add MX record: Name '@', Server 'server.hostnextdns.com', Priority 10 to receive incoming bounces and support replies.`
      );
    }

    const isInboxReady = spfExists && spfIncludesHost && dmarcExists;

    const result: DnsCheckResult = {
      domain: queryDomain,
      spf: {
        exists: spfExists,
        record: spfRecord,
        isAuthorizedForHost: spfIncludesHost,
        expectedRecord: expectedSpf,
        status: spfExists && spfIncludesHost ? "valid" : spfExists ? "warning" : "missing",
        message: spfExists
          ? spfIncludesHost
            ? "SPF is valid and authorizes mail server 5.39.69.62"
            : "SPF exists but does not authorize server.hostnextdns.com (5.39.69.62)"
          : "Missing SPF record. Gmail/Yahoo will route outgoing mail to SPAM.",
      },
      dmarc: {
        exists: dmarcExists,
        record: dmarcRecord,
        expectedRecord: expectedDmarc,
        status: dmarcExists ? "valid" : "missing",
        message: dmarcExists
          ? "DMARC authentication policy is active"
          : "Missing DMARC policy on _dmarc domain. Mandatory for Google & Yahoo 2024 compliance.",
      },
      mx: {
        exists: mxExists,
        records: mxRecords,
        expectedRecord: expectedMx,
        status: mxExists ? "valid" : "missing",
        message: mxExists
          ? `Found ${mxRecords.length} MX record(s)`
          : "No MX record detected. Reverse mail checks will fail.",
      },
      summary: {
        isInboxReady,
        spamRiskLevel,
        recommendations,
      },
    };

    return res.status(200).json({ success: true, data: result });
  } catch (error: any) {
    console.error("[DNS Check Error]", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to inspect DNS records.",
    });
  }
}
