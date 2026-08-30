/**
 * ESDispatch Web Audio Synthesizer Engine
 * Pure mathematical Web Audio API oscillators and ADSR envelopes.
 * Zero external .mp3/.wav asset dependencies.
 */

export type SoundCue =
  | 'dispatch_broadcast'
  | 'geofence_arrival'
  | 'escrow_settled'
  | 'tactile_click'
  | 'error_buzz'
  | 'card_swipe'
  | 'surge_alert';

class SoundEngineClass {
  private ctx: AudioContext | null = null;
  private isMuted: boolean = false;
  private volume: number = 0.5;

  private initCtx(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      if (AudioCtx) this.ctx = new AudioCtx();
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume().catch(() => {});
    }
    return this.ctx;
  }

  public setMuted(muted: boolean) {
    this.isMuted = muted;
  }

  public setVolume(vol: number) {
    this.volume = Math.max(0, Math.min(1, vol));
  }

  public playCue(cue: SoundCue): void {
    if (this.isMuted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    const t0 = ctx.currentTime;
    const masterGain = ctx.createGain();
    masterGain.gain.setValueAtTime(this.volume * 0.22, t0);
    masterGain.connect(ctx.destination);

    switch (cue) {
      case 'dispatch_broadcast':
        // Upward dual-tone sweep: 587.33Hz (D5) -> 880Hz (A5)
        this.playTone(ctx, masterGain, 587.33, 'sine', t0, 0.25, 0.5);
        this.playTone(ctx, masterGain, 880.0, 'triangle', t0 + 0.08, 0.35, 0.6);
        break;

      case 'geofence_arrival':
        // High-precision harmonic proximity ping (1046.5Hz / C6)
        this.playTone(ctx, masterGain, 1046.5, 'sine', t0, 0.4, 0.7);
        this.playTone(ctx, masterGain, 2093.0, 'sine', t0 + 0.04, 0.25, 0.3);
        break;

      case 'escrow_settled':
        // Ascending Triple-Chord Arpeggio: Db5 (554.37Hz) -> F5 (698.46Hz) -> Ab5 (830.61Hz)
        this.playTone(ctx, masterGain, 554.37, 'triangle', t0, 0.3, 0.5);
        this.playTone(ctx, masterGain, 698.46, 'triangle', t0 + 0.08, 0.35, 0.5);
        this.playTone(ctx, masterGain, 830.61, 'sine', t0 + 0.16, 0.5, 0.7);
        break;

      case 'tactile_click':
        // Crisp 25ms low-pass micro-click
        this.playTone(ctx, masterGain, 1200, 'sine', t0, 0.03, 0.4, true);
        break;

      case 'error_buzz':
        // Damped minor second dissonance pulse (180Hz + 195Hz)
        this.playTone(ctx, masterGain, 180, 'sawtooth', t0, 0.25, 0.4, true);
        this.playTone(ctx, masterGain, 195, 'sawtooth', t0, 0.25, 0.4, true);
        break;

      case 'card_swipe':
        // Subtle air glide (400Hz -> 800Hz)
        this.playTone(ctx, masterGain, 400, 'sine', t0, 0.12, 0.3);
        this.playTone(ctx, masterGain, 800, 'sine', t0 + 0.04, 0.15, 0.25);
        break;

      case 'surge_alert':
        // Resonant beacon alarm
        this.playTone(ctx, masterGain, 740, 'triangle', t0, 0.3, 0.6);
        this.playTone(ctx, masterGain, 932, 'triangle', t0 + 0.1, 0.4, 0.6);
        break;
    }
  }

  private playTone(
    ctx: AudioContext,
    dest: GainNode,
    freq: number,
    type: OscillatorType,
    start: number,
    dur: number,
    gainLevel: number,
    lowPass = false
  ) {
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, start);

    // Fast attack (10ms) -> Exponential decay to zero
    gain.gain.setValueAtTime(0.0001, start);
    gain.gain.linearRampToValueAtTime(gainLevel, start + 0.01);
    gain.gain.exponentialRampToValueAtTime(0.0001, start + dur);

    if (lowPass) {
      const filter = ctx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(900, start);
      osc.connect(filter);
      filter.connect(gain);
    } else {
      osc.connect(gain);
    }

    gain.connect(dest);
    osc.start(start);
    osc.stop(start + dur + 0.05);
  }
}

export const SoundEngine = new SoundEngineClass();
