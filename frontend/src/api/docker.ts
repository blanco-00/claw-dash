import request from '@/utils/request'

export interface DockerStatus {
  connected: boolean
  message: string
  timestamp: string
}

export interface Container {
  id: string
  name: string
  status: string
  image: string
  ports: string
  created: string
}

export interface DockerStats {
  containersRunning: number
  containersTotal: number
  images: number
  volumes: number
  networks: number
}

export function getDockerStatus() {
  return request.get<DockerStatus>('/api/docker/status')
}

export function getContainers() {
  return request.get<Container[]>('/api/docker/containers')
}

export function getImages() {
  return request.get<any[]>('/api/docker/images')
}

export function getDockerStats() {
  return request.get<DockerStats>('/api/docker/stats')
}
