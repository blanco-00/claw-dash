export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageRequest {
  pageNum: number
  pageSize: number
  sortField?: string
  sortOrder?: string
}

export interface PageResponse<T> {
  total: number
  pageNum: number
  pageSize: number
  pages: number
  records: T[]
}
