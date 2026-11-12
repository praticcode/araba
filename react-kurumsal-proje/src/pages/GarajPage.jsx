import EntityManagementPage from '../components/EntityManagementPage.jsx'
import { arabaApi, garajApi } from '../services/api.js'

const loadOptions = () => arabaApi.list()

const fields = (options) => [
  {
    name: 'arabaId',
    label: 'Personel aracı',
    type: 'select',
    required: true,
    transform: (value) => Number(value),
    options: options.map((araba) => ({
      value: araba.id,
      label: `${araba.plaka} — ${araba.marka} ${araba.model} · ${araba.personelAd ?? `Personel #${araba.personelId}`}`,
    })),
  },
  { name: 'girisTarihi', label: 'Giriş tarihi', type: 'date', required: true },
  { name: 'girisSaati', label: 'Giriş saati', type: 'time', required: true },
  {
    name: 'cikisTarihi',
    label: 'Çıkış tarihi',
    type: 'date',
    transform: (value) => value || null,
  },
  {
    name: 'cikisSaati',
    label: 'Çıkış saati',
    type: 'time',
    transform: (value) => value || null,
  },
]

const formatDate = (value) => {
  if (!value) return '—'
  const [year, month, day] = value.split('-')
  return `${day}.${month}.${year}`
}

const columns = [
  {
    key: 'personelAd',
    label: 'Araç sahibi personel',
    render: (record) => `${record.personelAd} ${record.personelSoyad}`,
  },
  { key: 'plaka', label: 'Araç plakası' },
  { key: 'girisTarihi', label: 'Giriş tarihi', render: (record) => formatDate(record.girisTarihi) },
  { key: 'girisSaati', label: 'Giriş saati' },
  { key: 'cikisTarihi', label: 'Çıkış tarihi', render: (record) => formatDate(record.cikisTarihi) },
  { key: 'cikisSaati', label: 'Çıkış saati', render: (record) => record.cikisSaati || '—' },
  {
    key: 'durum',
    label: 'Durum',
    render: (record) => record.cikisTarihi ? 'Çıkış yaptı' : 'Garajda',
  },
]

export default function GarajPage() {
  return (
    <EntityManagementPage
      title="Garaj"
      description="Personel ve araçların garaj giriş-çıkış kayıtlarını takip edin."
      singular="Garaj kaydı"
      api={garajApi}
      fields={fields}
      columns={columns}
      loadOptions={loadOptions}
      emptyMessage="Henüz garaj giriş kaydı bulunmuyor."
    />
  )
}
