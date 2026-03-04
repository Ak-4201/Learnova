import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { AuthProvider, useAuth } from '../context/AuthContext'

vi.mock('../api/auth')

function TestConsumer() {
  const { user, isAuthenticated, logout } = useAuth()
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'yes' : 'no'}</span>
      {user && <span data-testid="name">{user.fullName}</span>}
      <button onClick={logout}>Logout</button>
    </div>
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('starts unauthenticated when no stored token', () => {
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    )
    expect(screen.getByTestId('auth')).toHaveTextContent('no')
  })

  it('shows authenticated and user when token and user in localStorage', () => {
    localStorage.setItem('token', 'jwt-xyz')
    localStorage.setItem('user', JSON.stringify({ email: 'a@b.com', fullName: 'Test User', role: 'STUDENT', userId: 1 }))
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    )
    expect(screen.getByTestId('auth')).toHaveTextContent('yes')
    expect(screen.getByTestId('name')).toHaveTextContent('Test User')
  })

  it('logout clears auth state', async () => {
    localStorage.setItem('token', 'jwt-xyz')
    localStorage.setItem('user', JSON.stringify({ email: 'a@b.com', fullName: 'Test User', role: 'STUDENT', userId: 1 }))
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    )
    expect(screen.getByTestId('auth')).toHaveTextContent('yes')
    const { act } = await import('@testing-library/react')
    await act(async () => { screen.getByText('Logout').click() })
    await waitFor(() => expect(screen.getByTestId('auth')).toHaveTextContent('no'))
  })
})
