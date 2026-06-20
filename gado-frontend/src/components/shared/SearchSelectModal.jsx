import { useMemo, useState } from 'react'

/**
 * Modal de busca e seleção reutilizável.
 *
 * Props:
 *  title        — string: título do modal
 *  items        — { id, ...campos }[]: lista de objetos disponíveis (id obrigatório)
 *  selectedIds  — id[]: seleção atual (número ou string)
 *  onConfirm    — fn(ids[]) → chamado ao confirmar
 *  onClose      — fn() → chamado ao cancelar ou fechar
 *  multiSelect  — boolean (default true): permite múltipla seleção
 *  columns      — { key, label }[]: colunas a exibir na tabela
 *  filterField  — opcional: { label, key, options: { value, label }[] }
 *  disabledIds  — opcional: id[] — itens que não podem ser selecionados nem desmarcados
 */
function SearchSelectModal({
  title,
  items,
  selectedIds,
  onConfirm,
  onClose,
  multiSelect = true,
  columns,
  filterField,
  disabledIds,
}) {
  const [search, setSearch] = useState('')
  const [filterValue, setFilterValue] = useState('')
  const [localSelected, setLocalSelected] = useState(() => [...selectedIds])

  const filtered = useMemo(() => {
    const term = search.toLowerCase().trim()
    return items.filter((item) => {
      if (term) {
        const match = columns.some((col) =>
          String(item[col.key] ?? '').toLowerCase().includes(term),
        )
        if (!match) return false
      }
      if (filterField && filterValue) {
        if (String(item[filterField.key] ?? '') !== filterValue) return false
      }
      return true
    })
  }, [items, search, filterValue, columns, filterField])

  function isDisabled(id) {
    return Array.isArray(disabledIds) && disabledIds.includes(id)
  }

  function toggleItem(id) {
    if (isDisabled(id)) return
    if (multiSelect) {
      setLocalSelected((prev) =>
        prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
      )
    } else {
      setLocalSelected((prev) => (prev[0] === id ? [] : [id]))
    }
  }

  return (
    <div className="ssm-overlay" role="dialog" aria-modal="true" aria-label={title}>
      <div className="ssm-box">
        <div className="ssm-header">
          <h3 className="ssm-title">{title}</h3>
          <button
            type="button"
            className="ssm-close"
            onClick={onClose}
            aria-label="Fechar"
          >
            ✕
          </button>
        </div>

        <div className="ssm-toolbar">
          <input
            type="text"
            className="ssm-search"
            placeholder="Buscar por código ou nome..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            autoFocus
          />
          {filterField ? (
            <select
              className="ssm-filter"
              value={filterValue}
              onChange={(e) => setFilterValue(e.target.value)}
              aria-label={filterField.label}
            >
              <option value="">{filterField.label}: Todos</option>
              {filterField.options.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          ) : null}
        </div>

        <div className="ssm-list-wrapper">
          <table className="ssm-table">
            <thead>
              <tr>
                <th className="ssm-th-check" />
                {columns.map((col) => (
                  <th key={col.key} className="ssm-th">
                    {col.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={columns.length + 1} className="ssm-empty">
                    Nenhum resultado encontrado.
                  </td>
                </tr>
              ) : (
                filtered.map((item) => {
                  const isSelected = localSelected.includes(item.id)
                  const itemDisabled = isDisabled(item.id)
                  return (
                    <tr
                      key={item.id}
                      className={`ssm-row${isSelected ? ' ssm-row--selected' : ''}${itemDisabled ? ' ssm-row--disabled' : ''}`}
                      onClick={() => toggleItem(item.id)}
                    >
                      <td className="ssm-td-check">
                        <input
                          type={multiSelect ? 'checkbox' : 'radio'}
                          checked={isSelected}
                          onChange={() => toggleItem(item.id)}
                          onClick={(e) => e.stopPropagation()}
                          disabled={itemDisabled}
                          style={{ accentColor: '#4f7f2f' }}
                          aria-label={`Selecionar ${String(item[columns[0]?.key] ?? item.id)}`}
                        />
                      </td>
                      {columns.map((col) => (
                        <td key={col.key} className="ssm-td">
                          {String(item[col.key] ?? '—')}
                        </td>
                      ))}
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>

        <div className="ssm-footer">
          <span className="ssm-count">
            {localSelected.length > 0
              ? `${localSelected.length} selecionado(s)`
              : 'Nenhum selecionado'}
          </span>
          <div className="ssm-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button
              type="button"
              className="btn-primary"
              onClick={() => onConfirm(localSelected)}
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default SearchSelectModal
