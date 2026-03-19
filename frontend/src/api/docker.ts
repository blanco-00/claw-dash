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
  return request.get<DockerStatus>('/docker/status')
}

export function getContainers() {
  return request.get<Container[]>('/docker/containers')
}

export function getImages() {
  return request.get<any[]>('/docker/images')
}

export function getDockerStats() {
  return request.get<DockerStats>('/docker/stats')
}
