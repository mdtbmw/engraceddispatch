import { NextResponse } from 'next/server';
import { getApps, initializeApp, cert } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { getFirestore } from 'firebase-admin/firestore';
import * as path from 'path';

if (!getApps().length) {
  let credential;
  if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    try {
      const parsed = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
      credential = cert(parsed);
    } catch {
      const serviceAccountPath = path.resolve(process.cwd(), 'functions/service-account.json');
      credential = cert(serviceAccountPath);
    }
  } else {
    const serviceAccountPath = path.resolve(process.cwd(), 'functions/service-account.json');
    credential = cert(serviceAccountPath);
  }
  initializeApp({
    credential,
    projectId: 'engraceddispatch-ffba4',
  });
}

export async function POST(request: Request) {
  try {
    const { idToken } = await request.json();
    if (!idToken) {
      return NextResponse.json({ valid: false }, { status: 401 });
    }
    const decoded = await getAuth().verifyIdToken(idToken);
    const userDoc = await getFirestore().collection('users').doc(decoded.uid).get();
    const role = userDoc.data()?.role || '';
    const allowedRoles = ['admin', 'super_admin', 'dispatcher'];
    if (!allowedRoles.includes(role)) {
      return NextResponse.json({ valid: false, role }, { status: 403 });
    }
    return NextResponse.json({ valid: true, uid: decoded.uid, role });
  } catch {
    return NextResponse.json({ valid: false }, { status: 401 });
  }
}
