export default function LoadingSpinner({ className = 'h-10 w-10' }: { className?: string }) {
  return (
    <div className="flex items-center justify-center">
      <div
        className={`${className} border-2 border-primary border-t-transparent rounded-full animate-spin`}
      />
    </div>
  );
}
