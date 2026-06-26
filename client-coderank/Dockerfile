# -------- Stage 1: Install Dependencies --------
FROM node:22-alpine AS deps

WORKDIR /app

COPY package*.json ./

RUN npm ci

# -------- Stage 2: Build --------
FROM node:22-alpine AS builder

WORKDIR /app

# Pass API URL as a build argument so Next.js can bake it in at build time
ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL

COPY --from=deps /app/node_modules ./node_modules
COPY . .

RUN npm run build

# -------- Stage 3: Production Runtime --------
FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy only what's needed to run the app (standalone output is much smaller)
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

EXPOSE 3000

CMD ["node", "server.js"]
