interface PaginationControlsProps {
  page: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  itemLabel: string;
  onPageChange: (page: number) => void;
}

function buildVisiblePages(currentPage: number, totalPages: number) {
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, index) => index);
  }

  const start = Math.max(0, Math.min(currentPage - 1, totalPages - 5));
  return Array.from({ length: 5 }, (_, index) => start + index);
}

export function PaginationControls({
  page,
  totalPages,
  totalElements,
  pageSize,
  itemLabel,
  onPageChange
}: PaginationControlsProps) {
  if (totalPages <= 1) {
    return null;
  }

  const visiblePages = buildVisiblePages(page, totalPages);
  const from = page * pageSize + 1;
  const to = Math.min(totalElements, (page + 1) * pageSize);

  return (
    <div className="pagination-shell">
      <p className="pagination-summary">
        Mostrando {from}-{to} de {totalElements} {itemLabel}
      </p>

      <div className="pagination-controls" aria-label={`Paginacion de ${itemLabel}`}>
        <button
          className="secondary-button inline-button"
          type="button"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
        >
          Anterior
        </button>

        <div className="pagination-pages">
          {visiblePages.map((visiblePage) => (
            <button
              key={visiblePage}
              className={visiblePage === page ? "primary-button inline-button pagination-button" : "secondary-button inline-button pagination-button"}
              type="button"
              onClick={() => onPageChange(visiblePage)}
              aria-current={visiblePage === page ? "page" : undefined}
            >
              {visiblePage + 1}
            </button>
          ))}
        </div>

        <button
          className="secondary-button inline-button"
          type="button"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
        >
          Siguiente
        </button>
      </div>
    </div>
  );
}
