import { Injectable } from '@angular/core'

@Injectable({
  providedIn: 'root',
})
export class ConfettiService {
  constructor() {}

  /**
   * Shoots confetti
   */
  shoot() {
    const confetti = (window as any)['confetti']
    return confetti.apply(this, {
      angle: this.random(60, 120),
      spread: this.random(10, 50),
      particleCount: this.random(40, 50),
      origin: {
        y: 0.6,
      },
    })
  }

  private random(min: number, max: number) {
    return Math.random() * (max - min) + min
  }
}
