const BASE_URL = ''

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  headers?: Record<string, string>
}

async function request<T = unknown>(url: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, headers = {} } = options

  const config: RequestInit = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  }

  if (body) {
    config.body = JSON.stringify(body)
  }

  const response = await fetch(`${BASE_URL}${url}`, config)

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }

  return response.json()
}

export const http = {
  get<T = unknown>(url: string) {
    return request<T>(url)
  },
  post<T = unknown>(url: string, body?: unknown) {
    return request<T>(url, { method: 'POST', body })
  },
  put<T = unknown>(url: string, body?: unknown) {
    return request<T>(url, { method: 'PUT', body })
  },
  delete<T = unknown>(url: string) {
    return request<T>(url, { method: 'DELETE' })
  }
}

export default http
