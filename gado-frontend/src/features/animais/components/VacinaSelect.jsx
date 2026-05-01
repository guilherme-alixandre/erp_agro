import { useEffect, useMemo, useRef, useState } from 'react'

function VacinaSelect({ value, vacinasDisponiveis, onChange, placeholder }) {
  const [open, setOpen] = useState(false)
  const containerRef = useRef(null)

  const lista = Array.isArray(vacinasDisponiveis) ? vacinasDisponiveis : []

  const filtradas = useMemo(() => {
    const termo = String(value ?? '').trim().toLowerCase()
    if (!termo) return lista
    return lista.filter((v) =>
      String(v.nome ?? '').toLowerCase().includes(termo),
    )
  }, [lista, value])

  useEffect(() => {
    function handleClickOutside(event) {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target)
      ) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleInputChange(event) {
    onChange(event.target.value)
    setOpen(true)
  }

  function handleSelect(vacina) {
    onChange(vacina.nome)
    setOpen(false)
  }

  function handleBlur() {
    const termo = String(value ?? '').trim()
    if (!termo) return

    const existe = lista.some(
      (v) => String(v.nome ?? '').toLowerCase() === termo.toLowerCase(),
    )
    if (existe) return

    const confirmar = window.confirm(
      `Uma vacina não cadastrada foi inserida ("${termo}"), deseja salvar mesmo assim?`,
    )
    if (!confirmar) {
      onChange('')
    }
  }

  return (
    <div className="vacina-select" ref={containerRef}>
      <input
        type="text"
        value={value ?? ''}
        onChange={handleInputChange}
        onFocus={() => setOpen(true)}
        onBlur={handleBlur}
        placeholder={placeholder ?? 'Buscar vacina cadastrada'}
        autoComplete="off"
      />
      {open ? (
        <ul className="vacina-select__list">
          {filtradas.length ? (
            filtradas.map((vacina) => (
              <li key={vacina.id}>
                <button
                  type="button"
                  className="vacina-select__item"
                  onMouseDown={(event) => {
                    event.preventDefault()
                    handleSelect(vacina)
                  }}
                >
                  <span>{vacina.nome}</span>
                  {vacina.pendente ? (
                    <span className="vacina-badge vacina-badge--pendente">
                      Pendente
                    </span>
                  ) : null}
                </button>
              </li>
            ))
          ) : (
            <li className="vacina-select__empty">
              Nenhuma vacina cadastrada com esse nome
            </li>
          )}
        </ul>
      ) : null}
    </div>
  )
}

export default VacinaSelect
