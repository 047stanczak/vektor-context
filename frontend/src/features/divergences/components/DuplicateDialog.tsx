import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

interface Props {
  open: boolean
  productCode: number
  tipo: string
  onSum: () => void
  onNew: () => void
  onCancel: () => void
}

export default function DuplicateDialog({ open, productCode, tipo, onSum, onNew, onCancel }: Props) {
  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onCancel()}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Item duplicado</DialogTitle>
          <DialogDescription>
            O produto <strong>{productCode}</strong> ({tipo}) já está na lista. O que deseja fazer?
          </DialogDescription>
        </DialogHeader>

        <DialogFooter className="flex-col gap-2 sm:flex-col">
          <Button className="w-full" onClick={onSum}>Somar quantidade</Button>
          <Button variant="secondary" className="w-full" onClick={onNew}>Criar linha separada</Button>
          <Button variant="ghost" className="w-full" onClick={onCancel}>Cancelar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
