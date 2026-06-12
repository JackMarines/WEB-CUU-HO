'use client';

interface Props {
  status: string;
}

const STATUS_MAP: Record<string, { label: string; className: string }> = {
  active: {
    label: 'ACTIVE',
    className: 'bg-primary-subtle text-status-high',
  },
  in_progress: {
    label: 'IN PROGRESS',
    className: 'bg-[rgba(230,81,0,0.15)] text-status-medium',
  },
  resolved: {
    label: 'RESOLVED',
    className: 'bg-[rgba(46,125,50,0.15)] text-status-resolved',
  },
};

export default function StatusBadge({ status }: Props) {
  const s = STATUS_MAP[status] || STATUS_MAP.active;
  return (
    <span
      className={`inline-block text-[10px] font-semibold tracking-wide px-[10px] py-[3px] rounded-tag transition-all duration-200 ease-out ${s.className}`}
    >
      {s.label}
    </span>
  );
}
