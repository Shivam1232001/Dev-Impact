"use client"

import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"

interface ContributorData {
  name: string
  percentage: number
  commits: number
  color: string
}

interface ContributorChartProps {
  data: ContributorData[]
}

const COLORS = ["#8b5cf6", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#3b82f6", "#ec4899", "#84cc16"]

export function ContributorChart({ data }: ContributorChartProps) {
  console.log("[v0] Chart data received:", data)

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload
      return (
        <div className="bg-card border border-border rounded-lg p-3 shadow-lg">
          <p className="font-medium text-foreground">{data.name}</p>
          <p className="text-sm text-muted-foreground">
            {data.commits} commits ({data.percentage}%)
          </p>
        </div>
      )
    }
    return null
  }

  const CustomLegend = ({ payload }: any) => {
    return (
      <div className="flex flex-wrap justify-center gap-4 mt-4">
        {payload.map((entry: any, index: number) => (
          <div key={index} className="flex items-center">
            <div className="w-3 h-3 rounded-full mr-2" style={{ backgroundColor: COLORS[index % COLORS.length] }} />
            <span className="text-sm text-foreground">{entry.value}</span>
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="w-full h-80">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            outerRadius={80}
            dataKey="percentage"
            label={({ percentage }) => `${percentage}%`}
            labelLine={false}
          >
            {data.map((entry, index) => {
              const color = COLORS[index % COLORS.length]
              console.log(`[v0] Cell ${index}: using color=${color}`)
              return <Cell key={`cell-${index}`} fill={color} />
            })}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
          <Legend content={<CustomLegend />} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
