export interface ErrorDto {
  timestamp: string
  status: number
  error: string
  path: string
}

export namespace ErrorDto {
  export function is(value: ErrorDto | any): value is ErrorDto {
    return (
      !!value &&
      typeof value === 'object' &&
      typeof value.timestamp === 'string' &&
      typeof value.status === 'number' &&
      typeof value.error === 'string' &&
      typeof value.path === 'string'
    )
  }
}
