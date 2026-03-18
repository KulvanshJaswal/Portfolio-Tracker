# Portfolio Tracker API Endpoints

## Users
- POST /api/users - Create user
- GET /api/users/{userId} - Get user by ID
- PUT /api/users/{userId}/username - Update username
- PUT /api/users/{userId}/email - Update email
- DELETE /api/users/{userId} - Delete user (fails if user owns portfolios)

## Portfolios
- POST /api/users/{userId}/portfolios - Create portfolio for user
- GET /api/users/{userId}/portfolios - List all portfolios for user
- GET /api/portfolios/{portfolioId} - Get single portfolio by ID
- PUT /api/portfolios/{portfolioId} - Update portfolio name
- DELETE /api/portfolios/{portfolioId} - Delete portfolio

## Transactions
- POST /api/portfolios/{portfolioId}/transactions/deposit - Deposit cash
- POST /api/portfolios/{portfolioId}/transactions/withdraw - Withdraw cash
- POST /api/portfolios/{portfolioId}/transactions/buy - Buy stock/crypto (fetches live price from API)
- POST /api/portfolios/{portfolioId}/transactions/sell - Sell stock/crypto (fetches live price from API)

## Positions
- GET /api/portfolios/{portfolioId}/positions - Get all positions in portfolio
- GET /api/portfolios/{portfolioId}/positions/{symbol} - Get specific position by symbol
- GET /api/portfolios/{portfolioId}/positions/{symbol}/pnl - Get position profit/loss (with live price)

## Features
✅ Live stock prices via Alpha Vantage API (stocks and crypto)
✅ Real-time P&L calculations
✅ Complete transaction history
✅ Multi-portfolio support per user