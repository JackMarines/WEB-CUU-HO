'use client';

import { useEffect, useRef, useState } from 'react';

export function useCallEvents() {
  const [newCallCount, setNewCallCount] = useState(0);
  const [lastCall, setLastCall] = useState<any>(null);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const es = new EventSource(`http://localhost:8080/api/events/subscribe?token=${token}`);
    eventSourceRef.current = es;

    es.addEventListener('new-call', (e) => {
      try {
        const call = JSON.parse(e.data);
        setLastCall(call);
        setNewCallCount(prev => prev + 1);
      } catch {
        // ignore parse errors
      }
    });

    es.onerror = () => {
      es.close();
    };

    return () => {
      es.close();
    };
  }, []);

  function resetCount() {
    setNewCallCount(0);
  }

  return { newCallCount, lastCall, resetCount };
}
