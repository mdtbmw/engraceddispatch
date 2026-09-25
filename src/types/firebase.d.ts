declare module 'firebase/app' {
  export interface FirebaseApp {
    name: string;
    options: Record<string, any>;
  }
  export function initializeApp(options: Record<string, any>, name?: string): FirebaseApp;
  export function getApps(): FirebaseApp[];
  export function getApp(name?: string): FirebaseApp;
}

declare module 'firebase/auth' {
  export interface User {
    uid: string;
    email: string | null;
    displayName: string | null;
    photoURL: string | null;
    phoneNumber: string | null;
    emailVerified: boolean;
    getIdToken(forceRefresh?: boolean): Promise<string>;
  }

  export interface Auth {
    currentUser: User | null;
  }

  export function getAuth(app?: any): Auth;
  export function signInWithEmailAndPassword(auth: any, email: string, pass: string): Promise<any>;
  export function createUserWithEmailAndPassword(auth: any, email: string, pass: string): Promise<any>;
  export function signOut(auth: any): Promise<void>;
  export function onAuthStateChanged(auth: any, observer: (user: User | null) => void, error?: (err: any) => void): () => void;
  export function sendPasswordResetEmail(auth: any, email: string): Promise<void>;
  export function updateProfile(user: any, profile: { displayName?: string; photoURL?: string }): Promise<void>;
}

declare module 'firebase/firestore' {
  export interface Firestore {
    type: string;
    app: any;
  }

  export interface DocumentData {
    [field: string]: any;
  }

  export interface DocumentReference<T = DocumentData> {
    id: string;
    path: string;
    parent: any;
    converter: any;
  }

  export interface DocumentSnapshot<T = DocumentData> {
    id: string;
    ref: DocumentReference<T>;
    exists(): boolean;
    data(): any;
    get(fieldPath: string): any;
  }

  export interface QueryDocumentSnapshot<T = DocumentData> extends DocumentSnapshot<T> {
    data(): any;
  }

  export interface QuerySnapshot<T = DocumentData> {
    docs: QueryDocumentSnapshot<T>[];
    size: number;
    empty: boolean;
    forEach(callback: (result: QueryDocumentSnapshot<T>) => void): void;
  }

  export interface CollectionReference<T = DocumentData> {
    id: string;
    path: string;
  }

  export interface Query<T = DocumentData> {
    type: string;
  }

  export interface WriteBatch {
    set(documentRef: any, data: any, options?: any): WriteBatch;
    update(documentRef: any, data: any): WriteBatch;
    delete(documentRef: any): WriteBatch;
    commit(): Promise<void>;
  }

  export interface Transaction {
    get(documentRef: any): Promise<DocumentSnapshot>;
    set(documentRef: any, data: any, options?: any): Transaction;
    update(documentRef: any, data: any): Transaction;
    delete(documentRef: any): Transaction;
  }

  export interface FieldValue {
    isEqual(other: FieldValue): boolean;
  }

  export class Timestamp {
    seconds: number;
    nanoseconds: number;
    constructor(seconds: number, nanoseconds: number);
    static now(): Timestamp;
    static fromDate(date: Date): Timestamp;
    static fromMillis(milliseconds: number): Timestamp;
    toDate(): Date;
    toMillis(): number;
  }

  export function getFirestore(app?: any): Firestore;
  export function collection(firestore: any, ...pathSegments: string[]): any;
  export function doc(firestoreOrCollection: any, ...pathSegments: string[]): any;
  export function getDoc(reference: any): Promise<DocumentSnapshot>;
  export function getDocs(query: any): Promise<QuerySnapshot>;
  export function setDoc(reference: any, data: any, options?: any): Promise<void>;
  export function updateDoc(reference: any, data: any, ...moreFieldsAndValues: any[]): Promise<void>;
  export function deleteDoc(reference: any): Promise<void>;
  export function addDoc(reference: any, data: any): Promise<DocumentReference>;
  export function query(query: any, ...queryConstraints: any[]): any;
  export function where(fieldPath: string, opStr: any, value: any): any;
  export function orderBy(fieldPath: string, directionStr?: "asc" | "desc"): any;
  export function limit(limit: number): any;
  export function onSnapshot(referenceOrQuery: any, onNext: (snapshot: any) => void, onError?: (error: any) => void): () => void;
  export function writeBatch(firestore: any): WriteBatch;
  export function increment(n: number): FieldValue;
  export function serverTimestamp(): FieldValue;
  export function runTransaction<T>(firestore: any, updateFunction: (transaction: Transaction) => Promise<T>): Promise<T>;
}
