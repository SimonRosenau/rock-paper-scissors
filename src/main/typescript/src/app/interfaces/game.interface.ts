import { EntityBase } from './entity-base.interface'

export interface Game extends EntityBase {
  state: Game.State
  /**
   * The data structure already supports more than 2 players,
   *   but the UI assumes there are only 2 players for now.
   */
  players: Game.Player[]
  round: number
}

export namespace Game {
  export enum State {
    CREATED = 'CREATED',
    STARTED = 'STARTED',
    FINISHED = 'FINISHED',
    CANCELLED = 'CANCELLED',
  }

  export enum Hand {
    ROCK = 'ROCK',
    PAPER = 'PAPER',
    SCISSORS = 'SCISSORS',
  }

  export namespace Hand {
    export const values: Hand[] = [Hand.ROCK, Hand.PAPER, Hand.SCISSORS]
    export const random = (): Hand => values[Math.floor(Math.random() * values.length)]
    export function render(hand: Hand): string {
      switch (hand) {
        case Hand.ROCK:
          return '🪨'
        case Hand.PAPER:
          return '📄'
        case Hand.SCISSORS:
          return '✂️'
      }
    }
  }

  export type Player = Player.Computer | Player.User

  export namespace Player {
    interface Base {
      ready: boolean
      score: number
      selection: Hand | null
    }

    export interface Computer extends Base {
      type: 'computer'
      name: string
    }

    export interface User extends Base {
      type: 'user'
      userId: string
    }
  }
}
