export const testUsers = {
  admin: {
    id: 1,
    username: 'admin',
    password: 'admin123',
    name: '管理员',
    role: 'ADMIN',
    email: 'admin@migrametric.com',
    phone: '13800138000',
    status: 1,
  },
  user: {
    id: 2,
    username: 'testuser',
    password: 'test123',
    name: '测试用户',
    role: 'USER',
    email: 'user@migrametric.com',
    phone: '13900139000',
    status: 1,
  },
  disabledUser: {
    id: 3,
    username: 'disabled',
    password: 'disabled123',
    name: '已禁用用户',
    role: 'USER',
    email: 'disabled@migrametric.com',
    phone: '13700137000',
    status: 0,
  },
}

export function getTestToken(userId: number): string {
  return `test-token-for-user-${userId}`
}