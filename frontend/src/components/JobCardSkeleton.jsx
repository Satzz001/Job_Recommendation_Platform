export function JobCardSkeleton() {
  return (
    <div className="card flex gap-4">
      <div className="skeleton-block w-11 h-11" />
      <div className="flex-1">
        <div className="skeleton-block h-3.5 w-1/2 mb-2" />
        <div className="skeleton-block h-3 w-1/3 mb-3.5" />
        <div className="flex gap-1.5">
          <div className="skeleton-block h-5 w-20 rounded-full" />
          <div className="skeleton-block h-5 w-20 rounded-full" />
        </div>
      </div>
    </div>
  );
}
