import EntityManagementPage from '../components/EntityManagementPage.jsx'
import { personelApi } from '../services/api.js'

const fields = () => [
  { name: 'ad', label: 'Ad', required: true, maxLength: 100 },
  { name: 'soyad', label: 'Soyad', required: true, maxLength: 100 },
  { name: 'email', label: 'E-posta', type: 'email', required: true, maxLength: 150 },
]

const columns = [
  { key: 'ad', label: 'Ad' },
  { key: 'soyad', label: 'Soyad' },
  { key: 'email', label: 'E-posta' },
]

export default function PersonellerPage() {
  return (
    <EntityManagementPage
      title="Personeller"
      description="Personel kayıtlarını oluşturun, güncelleyin ve yönetin."
      singular="Personel"
      api={personelApi}
      fields={fields}
      columns={columns}
      emptyMessage="Henüz personel kaydı bulunmuyor."
    />
  )
}
