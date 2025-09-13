// Copies Vite build output into backend static resources
const fs = require('fs')
const path = require('path')

const src = path.resolve(__dirname, '../frontend/dist')
const dest = path.resolve(__dirname, '../backend/src/main/resources/static')

function copyDir(from, to) {
  if (!fs.existsSync(from)) return
  if (!fs.existsSync(to)) fs.mkdirSync(to, { recursive: true })
  for (const entry of fs.readdirSync(from)) {
    const srcPath = path.join(from, entry)
    const dstPath = path.join(to, entry)
    const stat = fs.statSync(srcPath)
    if (stat.isDirectory()) copyDir(srcPath, dstPath)
    else fs.copyFileSync(srcPath, dstPath)
  }
  console.log('Copied build to', to)
}

copyDir(src, dest)
