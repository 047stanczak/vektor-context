import { useState, useRef, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchSeparators } from '../api'
import { getRecentSeparators, addRecentSeparator } from '@/lib/utils'
import { ChevronDown } from 'lucide-react'

interface Props {
  value: string
  onChange: (v: string) => void
  onBlur?: () => void
}

export default function SeparatorCombobox({ value, onChange, onBlur }: Props) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  const { data: backendList = [] } = useQuery({
    queryKey: ['separators'],
    queryFn: fetchSeparators,
    staleTime: Infinity,
  })

  const options = backendList.length > 0 ? backendList : getRecentSeparators()
  const filtered = options.filter((s) => s.toLowerCase().includes(value.toLowerCase()))

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false)
        if (value) addRecentSeparator(value)
        onBlur?.()
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [value, onBlur])

  return (
    <div ref={ref} className="relative">
      <div className="relative">
        <input
          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm pr-8 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          value={value}
          onChange={(e) => { onChange(e.target.value); setOpen(true) }}
          onFocus={() => setOpen(true)}
          placeholder="Digite ou selecione..."
        />
        <ChevronDown className="absolute right-2 top-2.5 w-4 h-4 text-gray-400 pointer-events-none" />
      </div>
      {open && filtered.length > 0 && (
        <ul className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-48 overflow-auto">
          {filtered.map((s) => (
            <li
              key={s}
              className="px-3 py-2 text-sm hover:bg-blue-50 cursor-pointer"
              onMouseDown={(e) => {
                e.preventDefault()
                onChange(s)
                addRecentSeparator(s)
                setOpen(false)
              }}
            >
              {s}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
