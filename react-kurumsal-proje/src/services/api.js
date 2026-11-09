const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  })

  if (response.status === 204) return null

  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const validationMessages = Object.values(body?.validationErrors ?? {})
    const message = [body?.message, ...validationMessages].filter(Boolean).join(' ')
    throw new Error(message || `İstek başarısız oldu (${response.status}).`)
  }

  return body
}

function jsonBody(value) {
  return JSON.stringify(value)
}

export const personelApi = {
  list: () => request('/personeller'),
  listDetailed: () => request('/personeller/detayli'),
  search: (filters) => {
    const params = new URLSearchParams()
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) params.set(key, value)
    })
    return request(`/personeller/ara${params.size ? `?${params}` : ''}`)
  },
  create: (data) => request('/personeller', { method: 'POST', body: jsonBody(data) }),
  update: (id, data) => request(`/personeller/${id}`, { method: 'PUT', body: jsonBody(data) }),
  remove: (id) => request(`/personeller/${id}`, { method: 'DELETE' }),
}

export const arabaApi = {
  list: () => request('/arabalar'),
  create: (data) => request('/arabalar', { method: 'POST', body: jsonBody(data) }),
  update: (id, data) => request(`/arabalar/${id}`, { method: 'PUT', body: jsonBody(data) }),
  remove: (id) => request(`/arabalar/${id}`, { method: 'DELETE' }),
}
