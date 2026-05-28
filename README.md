# Portfolio Tracker

A full-stack multi-user portfolio management system for tracking stocks and cryptocurrencies with real-time price updates, transaction history, and invite-based collaboration.

## Overview

Portfolio Tracker lets users create and manage investment portfolios, buy and sell stocks and crypto at live market prices, track P&L, and invite others to collaborate. Access is controlled through a role-based membership system — each portfolio can have Admins, Members, and Visitors.

## Key Features

- **Authentication** — JWT-based stateless auth with registration and login
- **Portfolio management** — create multiple portfolios, rename or delete them
- **Trading** — buy and sell stocks and crypto at live Alpha Vantage prices; deposit and withdraw cash
- **Real-time P&L** — per-position and portfolio-level profit/loss based on cached market prices, updated daily at market close
- **Transaction history** — full audit trail of every buy, sell, deposit, and withdrawal
- **Invite system** — share portfolios via invite codes with configurable role, expiry, and max uses
- **Role-based access** — Admin (full control), Member (trade + view), Visitor (view only)
- **Automated price refresh** — scheduled job updates all tracked prices at 4:30 PM ET on weekdays

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security 7 |
| Database | MySQL 8 with Hibernate |
| Frontend | React 19, Vite, React Router |
| Price Data | Alpha Vantage API (stocks + crypto) |

## Running Locally

**Prerequisites:** Java 21+, Node 18+, MySQL 8

### Backend

```bash
# Set your DB credentials and Alpha Vantage API key in application.properties
./mvnw spring-boot:run
# Runs on http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
```

## API Overview

| Resource | Endpoints |
|---|---|
| Users | `POST /api/users` · `POST /api/users/login` |
| Portfolios | `GET/POST /api/users/{id}/portfolios/summary` · `PUT/DELETE /api/portfolios/{id}` |
| Positions | `GET /api/portfolios/{id}/positions` · `GET /{symbol}/pnl` |
| Transactions | `GET /api/portfolios/{id}/transactions` · `POST /deposit` · `/withdrawal` · `/buy` · `/sell` |
| Members | `GET/POST /api/portfolios/{id}/members` · `PUT/DELETE /{userId}` |
| Invites | `POST /api/portfolios/{id}/invites` · `POST /api/invites/{code}/redeem/{userId}` |

## Database Schema

7 tables: `users`, `portfolios`, `memberships`, `transactions`, `positions`, `price_quotes`, `invites`

The `memberships` table is the source of truth for portfolio access — it covers both owned and joined portfolios and enforces unique `(user_id, portfolio_id)` pairs.

## Project Status

In active development. Core features (auth, trading, invites, P&L, transaction history) are complete and tested end-to-end.

## Authors

Kulvansh Jaswal and Kuljot Jaswal — University of Calgary Software Engineering Students