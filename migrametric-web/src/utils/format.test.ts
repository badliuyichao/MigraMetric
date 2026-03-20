/**
 * 日期格式化工具单元测试
 */
import { describe, it, expect } from 'vitest'
import {
  formatDate,
  formatDateTime,
  formatTime,
  getToday,
  getYesterday,
  addDays,
  subtractDays,
  diffDays,
  isToday,
  isYesterday,
  isTomorrow
} from '@/utils/format'

describe('Date Format Utils', () => {
  describe('formatDate', () => {
    it('should format date correctly with default format', () => {
      const date = new Date('2026-03-19T10:30:00')
      expect(formatDate(date)).toBe('2026-03-19')
    })

    it('should format date with custom format', () => {
      const date = new Date('2026-03-19T10:30:00')
      expect(formatDate(date, 'YYYY/MM/DD')).toBe('2026/03/19')
      expect(formatDate(date, 'MM-DD-YYYY')).toBe('03-19-2026')
    })

    it('should format string date', () => {
      expect(formatDate('2026-03-19')).toBe('2026-03-19')
    })

    it('should format timestamp', () => {
      const timestamp = new Date('2026-03-19').getTime()
      expect(formatDate(timestamp)).toBe('2026-03-19')
    })
  })

  describe('formatDateTime', () => {
    it('should format datetime correctly', () => {
      const date = new Date('2026-03-19T10:30:00')
      expect(formatDateTime(date)).toBe('2026-03-19 10:30:00')
    })
  })

  describe('formatTime', () => {
    it('should format time correctly', () => {
      const date = new Date('2026-03-19T10:30:00')
      expect(formatTime(date)).toBe('10:30:00')
    })
  })

  describe('getToday', () => {
    it('should return today date in YYYY-MM-DD format', () => {
      const today = getToday()
      expect(today).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    })
  })

  describe('getYesterday', () => {
    it('should return yesterday date', () => {
      const yesterday = getYesterday()
      const today = getToday()
      const diff = diffDays(today, yesterday)
      expect(diff).toBe(1)
    })
  })

  describe('addDays', () => {
    it('should return date after adding days', () => {
      const today = getToday()
      const fiveDaysLater = addDays(5)
      const diff = diffDays(fiveDaysLater, today)
      expect(diff).toBe(5)
    })
  })

  describe('subtractDays', () => {
    it('should return date before subtracting days', () => {
      const today = getToday()
      const fiveDaysAgo = subtractDays(5)
      const diff = diffDays(today, fiveDaysAgo)
      expect(diff).toBe(5)
    })
  })

  describe('diffDays', () => {
    it('should calculate days difference correctly', () => {
      expect(diffDays('2026-03-20', '2026-03-15')).toBe(5)
      expect(diffDays('2026-03-15', '2026-03-20')).toBe(-5)
      expect(diffDays('2026-03-19', '2026-03-19')).toBe(0)
    })
  })

  describe('isToday', () => {
    it('should return true for today', () => {
      expect(isToday(new Date())).toBe(true)
      expect(isToday(getToday())).toBe(true)
    })

    it('should return false for other dates', () => {
      expect(isToday('2026-01-01')).toBe(false)
    })
  })

  describe('isYesterday', () => {
    it('should return true for yesterday', () => {
      expect(isYesterday(getYesterday())).toBe(true)
    })

    it('should return false for other dates', () => {
      expect(isYesterday(getToday())).toBe(false)
    })
  })

  describe('isTomorrow', () => {
    it('should return true for tomorrow', () => {
      expect(isTomorrow(addDays(1))).toBe(true)
    })

    it('should return false for other dates', () => {
      expect(isTomorrow(getToday())).toBe(false)
    })
  })
})
