import { Moon, Sun } from 'lucide-react'
import { useTheme } from '../theme/ThemeContext'

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()

  return (
    <button
      type="button"
      className="cg-theme-toggle"
      onClick={toggleTheme}
      aria-label={`Passa al tema ${theme === 'dark' ? 'chiaro' : 'scuro'}`}
    >
      {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
    </button>
  )
}
