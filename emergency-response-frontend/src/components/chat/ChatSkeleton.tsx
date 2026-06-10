'use client';

export default function ChatSkeleton() {
  return (
    <div className="space-y-4 px-4 py-4 animate-pulse">
      <div className="flex items-start gap-3">
        <div className="flex-1">
          <div className="h-3 w-12 bg-surface-elevated rounded mb-2" />
          <div className="h-10 w-3/5 bg-surface-elevated rounded-lg" />
        </div>
      </div>
      <div className="flex items-start gap-3 justify-end">
        <div className="flex-1 max-w-[70%]">
          <div className="h-3 w-10 bg-surface-elevated rounded mb-2 ml-auto" />
          <div className="h-12 w-full bg-surface-elevated rounded-lg" />
        </div>
      </div>
    </div>
  );
}
