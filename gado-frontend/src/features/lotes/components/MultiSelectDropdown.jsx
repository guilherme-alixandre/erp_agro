import { useEffect, useRef, useState } from 'react'

function MultiSelectDropdown({ options, selectedIds, onChange, placeholder }) {
  const [open, setOpen] = useState(false)
  const containerRef = useRef(null)

  useEffect(() => {
    function handleClickOutside(event) {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function toggleOption(id) {
    const exists = selectedIds.includes(id)
    const next = exists
      ? selectedIds.filter((x) => x !== id)
      : [...selectedIds, id]
    onChange(next)
  }

  const selectedLabels = options
    .filter((opt) => selectedIds.includes(opt.id))
    .map((opt) => opt.label)

  const triggerText =
    selectedLabels.length === 0
      ? placeholder
      : selectedLabels.length <= 2
        ? selectedLabels.join(', ')
        : `${selectedLabels.slice(0, 2).join(', ')} +${selectedLabels.length - 2}`

  return (
    <div className="multiselect" ref={containerRef}>
      <button
        type="button"
        className="multiselect__trigger"
        onClick={() => setOpen((prev) => !prev)}
        aria-expanded={open}
        aria-haspopup="listbox"
      >
        <span className={selectedLabels.length === 0 ? 'multiselect__placeholder' : ''}>
          {triggerText}
        </span>
        <span className="multiselect__arrow" aria-hidden="true">
          {open ? '▲' : '▼'}
        </span>
      </button>

      {open ? (
        <ul className="multiselect__list" role="listbox" aria-multiselectable="true">
          {options.length === 0 ? (
            <li className="multiselect__empty">Nenhuma opção disponível.</li>
          ) : (
            options.map((opt) => (
              <li key={opt.id} role="option" aria-selected={selectedIds.includes(opt.id)}>
                <label className="multiselect__item">
                  <input
                    type="checkbox"
                    checked={selectedIds.includes(opt.id)}
                    onChange={() => toggleOption(opt.id)}
                  />
                  <span>{opt.label}</span>
                </label>
              </li>
            ))
          )}
        </ul>
      ) : null}
    </div>
  )
}

export default MultiSelectDropdown
