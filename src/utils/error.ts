// 错误处理工具

export class AppError extends Error {
  code: string
  statusCode: number

  constructor(message: string, code = 'UNKNOWN', statusCode = 500) {
    super(message)
    this.name = 'AppError'
    this.code = code
    this.statusCode = statusCode
  }
}

export class NetworkError extends AppError {
  constructor(message: string) {
    super(message, 'NETWORK_ERROR', 503)
    this.name = 'NetworkError'
  }
}

export class NotFoundError extends AppError {
  constructor(message = 'Resource not found') {
    super(message, 'NOT_FOUND', 404)
    this.name = 'NotFoundError'
  }
}

/**
 * 错误处理装饰器
 */
export function withErrorHandling<T extends (...args: any[]) => any>(
  fn: T,
  errorMessage = 'Operation failed'
): T {
  return ((...args: any[]) => {
    try {
      return fn(...args)
    } catch (error) {
      if (error instanceof AppError) {
        throw error
      }
      console.error(`${errorMessage}:`, error)
      throw new AppError(errorMessage, 'INTERNAL_ERROR', 500)
    }
  }) as T
}

/**
 * 异步错误处理包装
 */
export async function withErrorHandlingAsync<T>(
  fn: () => Promise<T>,
  errorMessage = 'Operation failed'
): Promise<T> {
  try {
    return await fn()
  } catch (error) {
    if (error instanceof AppError) {
      throw error
    }
    console.error(`${errorMessage}:`, error)
    throw new AppError(errorMessage, 'INTERNAL_ERROR', 500)
  }
}

/**
 * 重试装饰器
 */
export async function withRetry<T>(
  fn: () => Promise<T>,
  maxRetries = 3,
  delay = 1000
): Promise<T> {
  let lastError: Error | undefined
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error as Error
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, delay * (i + 1)))
      }
    }
  }
  
  throw lastError
}

export default {
  AppError,
  NetworkError,
  NotFoundError,
  withErrorHandling,
  withErrorHandlingAsync,
  withRetry
}
