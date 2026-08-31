import { AppIcon } from './AppShell'

function ModuleHeader({ icon, eyebrow = 'Gestão da fazenda', title, description, metrics = [], children }) {
  return (
    <section className="module-header">
      <div className="module-header__identity">
        <span className="module-header__icon"><AppIcon name={icon} size={30} /></span>
        <div>
          <span className="module-header__eyebrow">{eyebrow}</span>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
      </div>

      {metrics.length > 0 ? (
        <div className="module-header__metrics" aria-label="Indicadores da página">
          {metrics.map((metric) => (
            <div className="module-metric" key={metric.label}>
              <strong>{metric.value}</strong>
              <span>{metric.label}</span>
            </div>
          ))}
        </div>
      ) : null}

      {children ? <div className="module-header__extra">{children}</div> : null}
    </section>
  )
}

export default ModuleHeader
