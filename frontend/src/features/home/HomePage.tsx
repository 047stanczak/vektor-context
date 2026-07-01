import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import {
  AlertTriangle, Upload, ClipboardList, History,
  TrendingUp, ScanLine, Package, Clock,
  Plus, Check, Trash2, ChevronDown, ChevronUp, X,
} from 'lucide-react'
import { fetchTasks, createTask, completeTask, deleteTask, Task, TaskRequest } from './api'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'

const shortcuts = [
  { to: '/vektor/divergences/new',     label: 'Registrar Divergência', icon: AlertTriangle, color: 'shortcut-blue' },
  { to: '/vektor/uploads',             label: 'Importar',               icon: Upload,        color: 'shortcut-violet' },
  { to: '/vektor/counting',            label: 'Contagem',               icon: ClipboardList, color: 'shortcut-cyan' },
  { to: '/vektor/divergences/history', label: 'Histórico',              icon: History,       color: 'shortcut-emerald' },
  { to: '/vektor/rankings',            label: 'Rankings',               icon: TrendingUp,    color: 'shortcut-amber' },
  { to: '/vektor/pending-by-barcode',  label: 'Buscar Código',          icon: ScanLine,      color: 'shortcut-rose' },
  { to: '/vektor/old-pending',         label: 'Pendências',             icon: Package,       color: 'shortcut-purple' },
  { to: '/vektor/jobs',                label: 'Jobs',                   icon: Clock,         color: 'shortcut-teal' },
]

const PRIORITY_LABEL: Record<string, string> = { LOW: 'Baixa', MEDIUM: 'Média', HIGH: 'Alta' }
const PRIORITY_CLASS: Record<string, string> = { LOW: 'priority-low', MEDIUM: 'priority-medium', HIGH: 'priority-high' }
const ENERGY_LABEL: Record<string, string> = { QUICK: 'Rápida', HEAVY: 'Pesada', BORING: 'Chata', FOCUS: 'Foco' }
const FREQ_LABEL: Record<string, string> = { ONCE: 'Uma vez', WEEKLY_1X: '1x/semana', WEEKLY_2X: '2x/semana', WEEKLY_3X: '3x/semana' }

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'Bom dia'
  if (h < 18) return 'Boa tarde'
  return 'Boa noite'
}

function formatDate() {
  return new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })
}

const emptyForm: TaskRequest = { title: '', priority: 'MEDIUM', energyLevel: 'QUICK', frequency: 'ONCE', tags: [] }

export default function HomePage() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<TaskRequest>(emptyForm)
  const [tagInput, setTagInput] = useState('')
  const [saving, setSaving] = useState(false)
  const [completing, setCompleting] = useState<number | null>(null)
  const [deleting, setDeleting] = useState<number | null>(null)

  useEffect(() => {
    fetchTasks().then(setTasks).finally(() => setLoading(false))
  }, [])

  async function handleCreate() {
    if (!form.title.trim()) return
    setSaving(true)
    try {
      const created = await createTask(form)
      setTasks(prev => [created, ...prev])
      setForm(emptyForm)
      setTagInput('')
      setShowForm(false)
    } finally {
      setSaving(false)
    }
  }

  async function handleComplete(task: Task) {
    setCompleting(task.id)
    try {
      const updated = await completeTask(task.id)
      setTasks(prev => prev.map(t => t.id === task.id ? updated : t))
    } finally {
      setCompleting(null)
    }
  }

  async function handleDelete(id: number) {
    setDeleting(id)
    try {
      await deleteTask(id)
      setTasks(prev => prev.filter(t => t.id !== id))
    } finally {
      setDeleting(null)
    }
  }

  function addTag() {
    const tag = tagInput.trim()
    if (!tag || form.tags.includes(tag)) return
    setForm(f => ({ ...f, tags: [...f.tags, tag] }))
    setTagInput('')
  }

  function removeTag(tag: string) {
    setForm(f => ({ ...f, tags: f.tags.filter(t => t !== tag) }))
  }

  const todo = tasks.filter(t => t.status === 'TODO')
  const done = tasks.filter(t => t.status === 'DONE')

  return (
    <div className="space-y-8 fade-in">

      <div>
        <p className="text-sm font-medium text-muted-foreground mb-1">
          {formatDate()}
        </p>
        <h1 className="text-2xl font-bold tracking-tight text-foreground">
          {getGreeting()}, Junior
        </h1>
      </div>

      <section>
        <p className="section-label mb-3">Atalhos</p>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {shortcuts.map(({ to, label, icon: Icon, color }) => (
            <NavLink
              key={to}
              to={to}
              className={cn('shortcut-card', color)}
            >
              <div className={cn('shortcut-icon shortcut-icon-colored')}>
                <Icon className="w-4 h-4" />
              </div>
              <span className="text-sm font-medium leading-tight text-foreground">
                {label}
              </span>
            </NavLink>
          ))}
        </div>
      </section>

      <section>
        <div className="flex items-center justify-between mb-3">
          <p className="section-label">
            Tarefas {!loading && `(${todo.length} pendentes)`}
          </p>
          <Button size="sm" onClick={() => setShowForm(v => !v)}>
            {showForm ? <ChevronUp className="w-3.5 h-3.5" /> : <Plus className="w-3.5 h-3.5" />}
            {showForm ? 'Cancelar' : 'Nova tarefa'}
          </Button>
        </div>

        {showForm && (
          <div className="card p-4 mb-3 space-y-3">
            <input
              className="field"
              placeholder="Título da tarefa..."
              value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              onKeyDown={e => e.key === 'Enter' && handleCreate()}
              autoFocus
            />
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <div>
                <label className="label">Prioridade</label>
                <select className="field" value={form.priority} onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}>
                  <option value="LOW">Baixa</option>
                  <option value="MEDIUM">Média</option>
                  <option value="HIGH">Alta</option>
                </select>
              </div>
              <div>
                <label className="label">Energia</label>
                <select className="field" value={form.energyLevel} onChange={e => setForm(f => ({ ...f, energyLevel: e.target.value }))}>
                  <option value="QUICK">Rápida</option>
                  <option value="HEAVY">Pesada</option>
                  <option value="BORING">Chata</option>
                  <option value="FOCUS">Foco</option>
                </select>
              </div>
              <div>
                <label className="label">Frequência</label>
                <select className="field" value={form.frequency} onChange={e => setForm(f => ({ ...f, frequency: e.target.value }))}>
                  <option value="ONCE">Uma vez</option>
                  <option value="WEEKLY_1X">1x/semana</option>
                  <option value="WEEKLY_2X">2x/semana</option>
                  <option value="WEEKLY_3X">3x/semana</option>
                </select>
              </div>
            </div>
            <div>
              <label className="label">Tags</label>
              <div className="flex gap-2">
                <input
                  className="field flex-1"
                  placeholder="Adicionar tag..."
                  value={tagInput}
                  onChange={e => setTagInput(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && addTag()}
                />
                <Button variant="secondary" onClick={addTag}>+</Button>
              </div>
              {form.tags.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mt-2">
                  {form.tags.map(tag => (
                    <span key={tag} className="tag">
                      {tag}
                      <X className="w-3 h-3 cursor-pointer" onClick={() => removeTag(tag)} />
                    </span>
                  ))}
                </div>
              )}
            </div>
            <Button onClick={handleCreate} disabled={saving || !form.title.trim()} className="w-full">
              {saving ? 'Salvando...' : 'Criar tarefa'}
            </Button>
          </div>
        )}

        {loading ? (
          <div className="loading-state card">
            Carregando...
          </div>
        ) : tasks.length === 0 ? (
          <div className="empty-state card">
            <ClipboardList className="w-8 h-8 text-muted-foreground/40" />
            <p className="text-sm">Nenhuma tarefa ainda</p>
          </div>
        ) : (
          <div className="space-y-2">
            {todo.map(task => (
              <TaskRow
                key={task.id}
                task={task}
                completing={completing === task.id}
                deleting={deleting === task.id}
                onComplete={() => handleComplete(task)}
                onDelete={() => handleDelete(task.id)}
              />
            ))}
            {done.length > 0 && (
              <>
                <p className="section-label pt-2 pb-1">
                  Concluídas ({done.length})
                </p>
                {done.map(task => (
                  <TaskRow
                    key={task.id}
                    task={task}
                    completing={false}
                    deleting={deleting === task.id}
                    onComplete={() => {}}
                    onDelete={() => handleDelete(task.id)}
                  />
                ))}
              </>
            )}
          </div>
        )}
      </section>
    </div>
  )
}

function TaskRow({ task, completing, deleting, onComplete, onDelete }: {
  task: Task
  completing: boolean
  deleting: boolean
  onComplete: () => void
  onDelete: () => void
}) {
  const isDone = task.status === 'DONE'
  const isRecurring = task.frequency !== 'ONCE'
  const progressPct = isRecurring && task.weeklyTarget > 0
    ? Math.min(100, (task.weeklyDone / task.weeklyTarget) * 100)
    : 0

  return (
    <div className={cn('card px-4 py-3 flex items-center gap-3 transition-all duration-150', isDone && 'task-row-done')}>
      <button
        onClick={onComplete}
        disabled={isDone || completing}
        className={cn(isDone ? 'task-check-done' : 'task-check')}
      >
        {(isDone || completing) && <Check className="w-3.5 h-3.5 text-green-600" />}
      </button>

      <div className="flex-1 min-w-0">
        <p className={cn('text-sm font-medium text-foreground truncate', isDone && 'line-through')}>
          {task.title}
        </p>
        <div className="flex items-center gap-2 mt-1 flex-wrap">
          <span className={cn('text-xs font-medium', PRIORITY_CLASS[task.priority])}>
            {PRIORITY_LABEL[task.priority]}
          </span>
          <span className="text-xs text-muted-foreground">
            {ENERGY_LABEL[task.energyLevel]}
          </span>
          {task.tags?.map(tag => (
            <span key={tag} className="tag">
              {tag}
            </span>
          ))}
        </div>

        {isRecurring && (
          <div className="mt-2">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs text-muted-foreground">
                {FREQ_LABEL[task.frequency]}
              </span>
              <span className={cn('text-xs font-medium', progressPct >= 100 ? 'text-green-600' : 'text-muted-foreground')}>
                {task.weeklyDone}/{task.weeklyTarget} essa semana
              </span>
            </div>
            <div className="progress-track">
              <div
                className={cn(progressPct >= 100 ? 'progress-bar-success' : 'progress-bar')}
                style={{ width: `${progressPct}%` }}
              />
            </div>
          </div>
        )}
      </div>

      <button
        onClick={onDelete}
        disabled={deleting}
        className="btn-icon-danger"
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  )
}
