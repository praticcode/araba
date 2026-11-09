import EntityManagementPage from '../components/EntityManagementPage.jsx'
import { arabaApi, personelApi } from '../services/api.js'

const fields = (personeller) => [
  { name: 'plaka', label: 'Plaka', required: true, maxLength: 20 },
  { name: 'marka', label: 'Marka', required: true, maxLength: 100 },
  { name: 'model', label: 'Model', required: true, maxLength: 100 },
  { name: 'aracCikisYili', label: 'Araç çıkış tarihi', type: 'date', transform: (value) => value || null },
  {
    name: 'personelId',
    label: 'Sorumlu personel',
    type: 'select',
    required: true,
    transform: (value) => Number(value),
    options: personeller.map((personel) => ({
      value: personel.id,
      label: `${personel.ad} ${personel.soyad} (#${personel.id})`,
    })),
  },
]

const columns = [
  { key: 'plaka', label: 'Plaka' },
  { key: 'marka', label: 'Marka' },
  { key: 'model', label: 'Model' },
  { key: 'aracCikisYili', label: 'Araç çıkış tarihi', render: (record) => record.aracCikisYili || '—' },
  { key: 'personelId', label: 'Personel ID', render: (record) => <span className="id-badge">#{record.personelId}</span> },
]

export default function ArabalarPage() {
  return (
    <EntityManagementPage
      title="Araçlar"
      description="Araç envanterini ve sorumlu personel atamalarını yönetin."
      singular="Araç"
      api={arabaApi}
      fields={fields}
      columns={columns}
      loadOptions={personelApi.list}
      emptyMessage="Henüz araç kaydı bulunmuyor."
    />
  )
}
