import { useEffect, useRef, useState } from 'react'
import { personelApi } from '../services/api.js'

const filterFields = [
  { name: 'id', label: 'Personel ID', type: 'number', placeholder: 'Örn. 12' },
  { name: 'ad', label: 'Ad', placeholder: 'Ada göre ara' },
  { name: 'soyad', label: 'Soyad', placeholder: 'Soyada göre ara' },
  { name: 'email', label: 'E-posta', type: 'email', placeholder: 'E-posta ara' },
  { name: 'arabaId', label: 'Araç ID', type: 'number', placeholder: 'Örn. 5' },
  { name: 'plaka', label: 'Plaka', placeholder: 'Plaka ara' },
  { name: 'marka', label: 'Marka', placeholder: 'Marka ara' },
  { name: 'model', label: 'Model', placeholder: 'Model ara' },
  { name: 'aracCikisYili', label: 'Araç çıkış tarihi', type: 'date' },
]

const initialFilters = Object.fromEntries(filterFields.map(({ name }) => [name, '']))

function displayDate(value) {
  if (!value) return '—'
  const [year, month, day] = value.split('-')
  return `${day}.${month}.${year}`
}

function makeRows(personeller) {
  return personeller.flatMap((personel) => {
    if (!personel.arabalar?.length) return [{ ...personel, araba: null }]
    return personel.arabalar.map((araba) => ({ ...personel, araba }))
  })
}

export default function DashboardPage() {
  const [filters, setFilters] = useState(initialFilters)
  const [personeller, setPersoneller] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [summary, setSummary] = useState(null)
  const [summaryError, setSummaryError] = useState('')
  const requestId = useRef(0)
  const rows = makeRows(personeller)
  const arabaCount = summary?.reduce((total, personel) => total + (personel.arabalar?.length ?? 0), 0) ?? 0

  useEffect(() => {
    personelApi.listDetailed()
      .then(setSummary)
      .catch((requestError) => setSummaryError(requestError.message))
  }, [])

  useEffect(() => {
    const currentRequestId = ++requestId.current
    const timer = window.setTimeout(async () => {
      setIsLoading(true)
      setError('')
      try {
        const result = await personelApi.search(filters)
        if (requestId.current === currentRequestId) setPersoneller(result)
      } catch (requestError) {
        if (requestId.current === currentRequestId) setError(requestError.message)
      } finally {
        if (requestId.current === currentRequestId) setIsLoading(false)
      }
    }, 250)

    return () => window.clearTimeout(timer)
  }, [filters])

  function updateFilter(event) {
    const { name, value } = event.target
    setFilters((current) => ({ ...current, [name]: value }))
  }

  return (
    <section className="page-section">
      <div className="page-heading dashboard-heading">
        <div>
          <div className="eyebrow">YÖNETİM PANELİ</div>
          <h1>Genel Bakış</h1>
          <p>Personel ve araç kayıtlarınızın güncel görünümü.</p>
        </div>
        <span className="updated-label"><span className="status-dot" /> Canlı veriler</span>
      </div>

      <div className="summary-grid">
        <article className="summary-card">
          <div className="summary-card-top"><span className="summary-icon people-icon">♙</span><span className="summary-trend">KAYITLI</span></div>
          <span className="summary-label">Personel</span>
          <strong>{summary ? summary.length : '—'}</strong>
          <span className="summary-note">Sistemdeki toplam personel</span>
        </article>
        <article className="summary-card">
          <div className="summary-card-top"><span className="summary-icon car-icon">▰</span><span className="summary-trend">ENVANTER</span></div>
          <span className="summary-label">Araç</span>
          <strong>{summary ? arabaCount : '—'}</strong>
          <span className="summary-note">Personele atanmış araçlar</span>
        </article>
        <article className="summary-card accent-card">
          <div className="summary-card-top"><span className="summary-icon chart-icon">↗</span><span className="summary-trend">DURUM</span></div>
          <span className="summary-label">Veri bağlantısı</span>
          <strong className="connection-value">{error || summaryError ? 'Kesildi' : isLoading || !summary ? 'Bağlanıyor' : 'Aktif'}</strong>
          <span className="summary-note">{error || summaryError || 'Backend ile senkronize'}</span>
        </article>
      </div>

      <section className="panel data-grid-panel">
        <div className="panel-heading grid-heading">
          <div>
            <div className="eyebrow">BİRLEŞİK KAYITLAR</div>
            <h2>Personel ve araçlar</h2>
            <p>Her personel ve araç alanında ayrı ayrı arama yapın.</p>
          </div>
          <span className="record-count">{isLoading ? 'Yükleniyor…' : `${rows.length} satır`}</span>
        </div>

        <div className="filter-panel">
          <div className="filter-title">
            <div><span className="filter-symbol" aria-hidden="true">⌕</span><strong>Arama ve filtreler</strong></div>
            <button type="button" className="text-button" onClick={() => setFilters({ ...initialFilters })}>Filtreleri temizle</button>
          </div>
          <div className="filter-grid">
            {filterFields.map((field) => (
              <label className="filter-field" key={field.name}>
                <span>{field.label}</span>
                <input
                  type={field.type ?? 'text'}
                  name={field.name}
                  value={filters[field.name]}
                  placeholder={field.placeholder ?? ''}
                  min={field.type === 'number' ? '1' : undefined}
                  onChange={updateFilter}
                />
              </label>
            ))}
          </div>
        </div>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="table-scroll">
          <table className="data-table grid-table">
            <thead>
              <tr>
                <th scope="col">Personel ID</th>
                <th scope="col">Ad</th>
                <th scope="col">Soyad</th>
                <th scope="col">E-posta</th>
                <th scope="col">Araç ID</th>
                <th scope="col">Plaka</th>
                <th scope="col">Marka</th>
                <th scope="col">Model</th>
                <th scope="col">Araç çıkış tarihi</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td className="table-state" colSpan="9">Kayıtlar yükleniyor…</td></tr>
              ) : error ? (
                <tr><td className="table-state" colSpan="9">Veriler alınamadı. Bağlantınızı kontrol edip tekrar deneyin.</td></tr>
              ) : rows.length === 0 ? (
                <tr><td className="table-state" colSpan="9">Filtrelerle eşleşen kayıt bulunamadı.</td></tr>
              ) : rows.map((row) => (
                <tr key={`${row.id}-${row.araba?.id ?? 'aracsiz'}`}>
                  <td><span className="id-badge">#{row.id}</span></td>
                  <td className="primary-cell">{row.ad}</td>
                  <td>{row.soyad}</td>
                  <td>{row.email}</td>
                  <td>{row.araba ? <span className="id-badge">#{row.araba.id}</span> : '—'}</td>
                  <td>{row.araba?.plaka ?? '—'}</td>
                  <td>{row.araba?.marka ?? '—'}</td>
                  <td>{row.araba?.model ?? '—'}</td>
                  <td>{displayDate(row.araba?.aracCikisYili)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="table-footer">PersonelVeArabalariResponse · Alan bazlı sunucu tarafı filtreleme</div>
      </section>
    </section>
  )
}
