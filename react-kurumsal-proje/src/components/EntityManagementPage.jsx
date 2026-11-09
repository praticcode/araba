import { useCallback, useEffect, useMemo, useState } from 'react'

function emptyValues(fields) {
  return Object.fromEntries(fields.map((field) => [field.name, '']))
}

export default function EntityManagementPage({
  title,
  description,
  singular,
  api,
  fields,
  columns,
  loadOptions,
  emptyMessage,
}) {
  const [records, setRecords] = useState([])
  const [options, setOptions] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [formError, setFormError] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [editingRecord, setEditingRecord] = useState(null)
  const [formValues, setFormValues] = useState({})
  const resolvedFields = useMemo(() => fields(options), [fields, options])

  const loadRecords = useCallback(async () => {
    setIsLoading(true)
    setLoadError('')
    try {
      const [nextRecords, nextOptions] = await Promise.all([
        api.list(),
        loadOptions ? loadOptions() : Promise.resolve([]),
      ])
      setRecords(nextRecords)
      setOptions(nextOptions)
    } catch (error) {
      setLoadError(error.message)
    } finally {
      setIsLoading(false)
    }
  }, [api, loadOptions])

  useEffect(() => {
    const timer = window.setTimeout(loadRecords, 0)
    return () => window.clearTimeout(timer)
  }, [loadRecords])

  function openCreateForm() {
    setEditingRecord(null)
    setFormValues(emptyValues(resolvedFields))
    setFormError('')
  }

  function openEditForm(record) {
    setEditingRecord(record)
    setFormValues(Object.fromEntries(
      resolvedFields.map((field) => [field.name, record[field.name] ?? '']),
    ))
    setFormError('')
  }

  function closeForm() {
    setEditingRecord(null)
    setFormValues({})
    setFormError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormError('')

    const payload = Object.fromEntries(resolvedFields.map((field) => [
      field.name,
      field.transform ? field.transform(formValues[field.name]) : formValues[field.name],
    ]))

    try {
      if (editingRecord) await api.update(editingRecord.id, payload)
      else await api.create(payload)
      closeForm()
      await loadRecords()
    } catch (error) {
      setFormError(error.message)
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(record) {
    if (!window.confirm(`${singular} #${record.id} kaydını silmek istediğinize emin misiniz?`)) return

    setLoadError('')
    try {
      await api.remove(record.id)
      await loadRecords()
    } catch (error) {
      setLoadError(error.message)
    }
  }

  return (
    <section className="page-section">
      <div className="page-heading">
        <div>
          <div className="eyebrow">KAYIT YÖNETİMİ</div>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
        <button className="button button-primary" type="button" onClick={openCreateForm}>
          <span aria-hidden="true">＋</span> Yeni {singular}
        </button>
      </div>

      {loadError && (
        <div className="alert alert-error" role="alert">
          <span>{loadError}</span>
          <button type="button" className="text-button" onClick={loadRecords}>Tekrar dene</button>
        </div>
      )}

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>{title} listesi</h2>
            <p>Toplam {records.length} kayıt</p>
          </div>
          <button className="button button-secondary" type="button" onClick={loadRecords} disabled={isLoading}>
            Yenile
          </button>
        </div>

        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">ID</th>
                {columns.map((column) => <th scope="col" key={column.key}>{column.label}</th>)}
                <th scope="col" className="actions-heading">İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td className="table-state" colSpan={columns.length + 2}>Kayıtlar yükleniyor…</td></tr>
              ) : records.length === 0 ? (
                <tr><td className="table-state" colSpan={columns.length + 2}>{emptyMessage}</td></tr>
              ) : records.map((record) => (
                <tr key={record.id}>
                  <td><span className="id-badge">#{record.id}</span></td>
                  {columns.map((column) => (
                    <td key={column.key}>{column.render ? column.render(record) : record[column.key]}</td>
                  ))}
                  <td>
                    <div className="row-actions">
                      <button className="icon-button" type="button" aria-label={`${singular} kaydını düzenle`} onClick={() => openEditForm(record)}>Düzenle</button>
                      <button className="icon-button danger" type="button" aria-label={`${singular} kaydını sil`} onClick={() => handleDelete(record)}>Sil</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {(editingRecord !== null || Object.keys(formValues).length > 0) && (
        <div className="modal-backdrop" onMouseDown={(event) => {
          if (event.target === event.currentTarget && !isSaving) closeForm()
        }}>
          <section className="form-dialog" role="dialog" aria-modal="true" aria-labelledby="form-title">
            <div className="dialog-heading">
              <div>
                <div className="eyebrow">{editingRecord ? 'KAYDI GÜNCELLE' : 'YENİ KAYIT'}</div>
                <h2 id="form-title">{editingRecord ? `${singular} düzenle` : `Yeni ${singular}`}</h2>
              </div>
              <button className="close-button" type="button" aria-label="Pencereyi kapat" onClick={closeForm}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-fields">
                {resolvedFields.map((field) => (
                  <label className="form-field" key={field.name}>
                    <span>{field.label}{field.required && <span className="required-mark"> *</span>}</span>
                    {field.type === 'select' ? (
                      <select
                        required={field.required}
                        value={formValues[field.name] ?? ''}
                        onChange={(event) => setFormValues({ ...formValues, [field.name]: event.target.value })}
                      >
                        <option value="">Seçiniz</option>
                        {field.options?.map((option) => (
                          <option key={option.value} value={option.value}>{option.label}</option>
                        ))}
                      </select>
                    ) : (
                      <input
                        type={field.type ?? 'text'}
                        required={field.required}
                        maxLength={field.maxLength}
                        value={formValues[field.name] ?? ''}
                        placeholder={field.placeholder ?? ''}
                        onChange={(event) => setFormValues({ ...formValues, [field.name]: event.target.value })}
                      />
                    )}
                  </label>
                ))}
              </div>
              {formError && <div className="alert alert-error" role="alert">{formError}</div>}
              <div className="dialog-actions">
                <button className="button button-secondary" type="button" onClick={closeForm} disabled={isSaving}>Vazgeç</button>
                <button className="button button-primary" type="submit" disabled={isSaving}>
                  {isSaving ? 'Kaydediliyor…' : 'Kaydet'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </section>
  )
}
