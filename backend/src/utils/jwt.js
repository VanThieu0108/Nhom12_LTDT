import { SignJWT, jwtVerify } from 'jose';
import { env } from '../config/env.js';

const key = new TextEncoder().encode(env.JWT_SECRET);

export async function signAccessToken(user) {
  return new SignJWT({ name: user.explorerName })
    .setProtectedHeader({ alg: 'HS256', typ: 'JWT' })
    .setSubject(user.id)
    .setIssuer(env.JWT_ISSUER)
    .setAudience(env.JWT_AUDIENCE)
    .setIssuedAt()
    .setExpirationTime(env.JWT_TTL)
    .sign(key);
}

export async function signAdminToken(username) {
  return new SignJWT({ role: 'admin' })
    .setProtectedHeader({ alg: 'HS256', typ: 'JWT' })
    .setSubject(username)
    .setIssuer(env.JWT_ISSUER)
    .setAudience(env.JWT_AUDIENCE)
    .setIssuedAt()
    .setExpirationTime(env.JWT_TTL)
    .sign(key);
}

export async function verifyAccessToken(token) {
  const { payload } = await jwtVerify(token, key, {
    issuer: env.JWT_ISSUER,
    audience: env.JWT_AUDIENCE,
    algorithms: ['HS256']
  });
  if (!payload.sub) throw new Error('Token subject is missing');
  return payload;
}
