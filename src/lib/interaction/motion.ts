/**
 * ESDispatch Web Motion Physics & Spring Tokens
 * Standardized spring easing and animation presets for the React/Vite web application.
 */

export const WebSpringPhysics = {
  // Snappy, immediate feedback for buttons and quick icon toggles
  touchPress: {
    transition: 'transform 0.15s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.15s ease'
  },
  // Soft elastic settle for cards, modal dialogs, and drawer expansions
  softElastic: {
    transition: 'all 0.28s cubic-bezier(0.16, 1, 0.3, 1)'
  },
  // Continuous pill motion across tabs
  snappyPill: {
    transition: 'all 0.24s cubic-bezier(0.2, 0, 0, 1)'
  },
  // Signature moment pulse
  signaturePulse: {
    animation: 'esdPulse 2.2s cubic-bezier(0.4, 0, 0.6, 1) infinite'
  }
};

/**
 * Trigger tactile sound & haptic feel on button press in browser
 */
export function triggerTactileClick(sound = true) {
  if (sound) {
    import('./SoundEngine').then(({ SoundEngine }) => {
      SoundEngine.playCue('tactile_click');
    });
  }
  if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
    try {
      navigator.vibrate(10);
    } catch (_e) {}
  }
}
