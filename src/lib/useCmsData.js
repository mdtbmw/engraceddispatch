"use client";
import { useState, useEffect, useCallback } from "react";
import { db } from "~/lib/firebase";
import { collection, query, orderBy, onSnapshot, addDoc, updateDoc, deleteDoc, doc, Timestamp } from "firebase/firestore";

export function useCmsCollection(collectionName, defaultOrder = "order") {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const q = query(collection(db, collectionName), orderBy(defaultOrder));
    const unsub = onSnapshot(q, (snap) => {
      setItems(snap.docs.map((d) => ({ id: d.id, ...d.data() })));
      setLoading(false);
    });
    return unsub;
  }, [collectionName, defaultOrder]);

  const addItem = useCallback(async (data) => {
    return addDoc(collection(db, collectionName), { ...data, createdAt: Timestamp.now() });
  }, [collectionName]);

  const updateItem = useCallback(async (id, data) => {
    return updateDoc(doc(db, collectionName, id), { ...data, updatedAt: Timestamp.now() });
  }, [collectionName]);

  const deleteItem = useCallback(async (id) => {
    return deleteDoc(doc(db, collectionName, id));
  }, [collectionName]);

  return { items, loading, addItem, updateItem, deleteItem };
}

export function useServices() {
  return useCmsCollection("cms_services");
}
export function useTeamMembers() {
  return useCmsCollection("cms_team");
}
export function useTestimonials() {
  return useCmsCollection("cms_testimonials");
}
export function useBlogPosts() {
  return useCmsCollection("cms_blog");
}
export function usePortfolioItems() {
  return useCmsCollection("cms_portfolio");
}
