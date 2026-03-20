/**
 * 表单验证工具函数单元测试
 */
import { describe, it, expect } from 'vitest'
import {
  validateUsername,
  validatePassword,
  validateEmail,
  validatePhone,
  validateUrl,
  validatePositiveInteger,
  validateNonNegative,
  validateCoefficient,
  validateRequired,
  validateMaxLength,
  validateMinLength
} from '@/utils/validate'

describe('Form Validation Utils', () => {
  describe('validateUsername', () => {
    it('should return true for valid username', () => {
      expect(validateUsername('admin')).toBe(true)
      expect(validateUsername('user_123')).toBe(true)
      expect(validateUsername('UserName')).toBe(true)
      expect(validateUsername('test123')).toBe(true)
    })

    it('should return false for invalid username', () => {
      expect(validateUsername('')).toBe(false)
      expect(validateUsername('ab')).toBe(false) // less than 4 chars
      expect(validateUsername('user@123')).toBe(false) // contains special char
      expect(validateUsername('user name')).toBe(false) // contains space
      expect(validateUsername('a'.repeat(21))).toBe(false) // more than 20 chars
    })
  })

  describe('validatePassword', () => {
    it('should return true for valid password', () => {
      expect(validatePassword('admin123')).toBe(true)
      expect(validatePassword('Pass@word1')).toBe(true)
      expect(validatePassword('123456')).toBe(true)
      expect(validatePassword('a'.repeat(20))).toBe(true) // exactly 20 chars
    })

    it('should return false for invalid password', () => {
      expect(validatePassword('12345')).toBe(false) // less than 6 chars
      expect(validatePassword('')).toBe(false)
      expect(validatePassword('a'.repeat(21))).toBe(false) // more than 20 chars
    })
  })

  describe('validateEmail', () => {
    it('should return true for valid email', () => {
      expect(validateEmail('test@example.com')).toBe(true)
      expect(validateEmail('user.name@domain.co.uk')).toBe(true)
      expect(validateEmail('test+tag@example.com')).toBe(true)
    })

    it('should return false for invalid email', () => {
      expect(validateEmail('invalid')).toBe(false)
      expect(validateEmail('test@')).toBe(false)
      expect(validateEmail('@example.com')).toBe(false)
      expect(validateEmail('test@.com')).toBe(false)
    })
  })

  describe('validatePhone', () => {
    it('should return true for valid phone number', () => {
      expect(validatePhone('13800138000')).toBe(true)
      expect(validatePhone('15912345678')).toBe(true)
      expect(validatePhone('18612345678')).toBe(true)
    })

    it('should return false for invalid phone number', () => {
      expect(validatePhone('12345678901')).toBe(false) // wrong prefix
      expect(validatePhone('1380013800')).toBe(false) // too short
      expect(validatePhone('138001380001')).toBe(false) // too long
      expect(validatePhone('abc')).toBe(false) // contains letters
    })
  })

  describe('validateUrl', () => {
    it('should return true for valid URL', () => {
      expect(validateUrl('https://www.example.com')).toBe(true)
      expect(validateUrl('http://example.com')).toBe(true)
      expect(validateUrl('https://example.com/path')).toBe(true)
    })

    it('should return false for invalid URL', () => {
      expect(validateUrl('not a url')).toBe(false)
      expect(validateUrl('')).toBe(false)
    })
  })

  describe('validatePositiveInteger', () => {
    it('should return true for positive integer', () => {
      expect(validatePositiveInteger(1)).toBe(true)
      expect(validatePositiveInteger(100)).toBe(true)
      expect(validatePositiveInteger('123')).toBe(true)
    })

    it('should return false for non-positive integer', () => {
      expect(validatePositiveInteger(0)).toBe(false)
      expect(validatePositiveInteger(-1)).toBe(false)
      expect(validatePositiveInteger(1.5)).toBe(false)
      expect(validatePositiveInteger('abc')).toBe(false)
    })
  })

  describe('validateNonNegative', () => {
    it('should return true for non-negative number', () => {
      expect(validateNonNegative(0)).toBe(true)
      expect(validateNonNegative(1)).toBe(true)
      expect(validateNonNegative(1.5)).toBe(true)
      expect(validateNonNegative('100')).toBe(true)
    })

    it('should return false for negative number', () => {
      expect(validateNonNegative(-1)).toBe(false)
      expect(validateNonNegative(-0.5)).toBe(false)
    })
  })

  describe('validateCoefficient', () => {
    it('should return true for valid coefficient (0.1-10.0)', () => {
      expect(validateCoefficient(0.1)).toBe(true)
      expect(validateCoefficient(1.0)).toBe(true)
      expect(validateCoefficient(10.0)).toBe(true)
      expect(validateCoefficient(5.5)).toBe(true)
    })

    it('should return false for invalid coefficient', () => {
      expect(validateCoefficient(0)).toBe(false)
      expect(validateCoefficient(0.05)).toBe(false) // less than 0.1
      expect(validateCoefficient(10.5)).toBe(false) // more than 10.0
      expect(validateCoefficient(-1)).toBe(false)
    })
  })

  describe('validateRequired', () => {
    it('should return true for non-empty value', () => {
      expect(validateRequired('hello')).toBe(true)
      expect(validateRequired('  hello  ')).toBe(true)
      expect(validateRequired('0')).toBe(true)
    })

    it('should return false for empty value', () => {
      expect(validateRequired('')).toBe(false)
      expect(validateRequired('   ')).toBe(false)
      expect(validateRequired(null)).toBe(false)
      expect(validateRequired(undefined)).toBe(false)
    })
  })

  describe('validateMaxLength', () => {
    it('should return true when value length is within limit', () => {
      expect(validateMaxLength('hello', 10)).toBe(true)
      expect(validateMaxLength('hello', 5)).toBe(true)
    })

    it('should return false when value length exceeds limit', () => {
      expect(validateMaxLength('hello', 3)).toBe(false)
      expect(validateMaxLength('hello', 4)).toBe(false)
    })
  })

  describe('validateMinLength', () => {
    it('should return true when value length meets minimum', () => {
      expect(validateMinLength('hello', 3)).toBe(true)
      expect(validateMinLength('hello', 5)).toBe(true)
    })

    it('should return false when value length is below minimum', () => {
      expect(validateMinLength('hello', 6)).toBe(false)
      expect(validateMinLength('hello', 10)).toBe(false)
    })
  })
})
