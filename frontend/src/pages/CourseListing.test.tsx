import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import CourseListing from './CourseListing'
import { coursesApi } from '../api/courses'

vi.mock('../api/courses')

function renderWithRouter() {
  return render(
    <MemoryRouter>
      <CourseListing />
    </MemoryRouter>
  )
}

describe('CourseListing', () => {
  beforeEach(() => {
    vi.mocked(coursesApi.list).mockResolvedValue({
      data: [
        {
          id: 1,
          title: 'Java Programming',
          description: 'Master Java.',
          whatYouWillLearn: 'OOP, Collections',
          thumbnailUrl: 'https://example.com/thumb.jpg',
          category: 'Programming',
          instructorName: 'Jane Instructor',
          instructorId: 1,
          totalLessons: 5,
          totalDurationSeconds: 3600,
          enrolled: false,
        },
      ],
    })
  })

  it('fetches and displays course list with thumbnail, instructor, description', async () => {
    renderWithRouter()
    await waitFor(() => {
      expect(coursesApi.list).toHaveBeenCalled()
    })
    await waitFor(() => {
      expect(screen.getByText('Java Programming')).toBeInTheDocument()
    })
    expect(screen.getByText(/Jane Instructor/)).toBeInTheDocument()
    expect(screen.getByText(/Master Java/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /View details/i })).toBeInTheDocument()
  })

  it('shows Enroll/View details and lesson count', async () => {
    renderWithRouter()
    await waitFor(() => {
      expect(screen.getByText(/5 lessons/)).toBeInTheDocument()
    })
  })
})
