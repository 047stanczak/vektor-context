import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(214,32%,91%)',
        input: 'hsl(214,32%,91%)',
        ring: 'hsl(221,83%,53%)',
        background: 'hsl(0,0%,100%)',
        foreground: 'hsl(222,47%,11%)',
        primary: { DEFAULT: 'hsl(221,83%,53%)', foreground: 'hsl(0,0%,100%)' },
        secondary: { DEFAULT: 'hsl(210,40%,96%)', foreground: 'hsl(222,47%,11%)' },
        muted: { DEFAULT: 'hsl(210,40%,96%)', foreground: 'hsl(215,16%,47%)' },
        destructive: { DEFAULT: 'hsl(0,84%,60%)', foreground: 'hsl(0,0%,100%)' },
        card: { DEFAULT: 'hsl(0,0%,100%)', foreground: 'hsl(222,47%,11%)' },
      },
      borderRadius: { lg: '0.5rem', md: '0.375rem', sm: '0.25rem' },
    },
  },
  plugins: [],
} satisfies Config
