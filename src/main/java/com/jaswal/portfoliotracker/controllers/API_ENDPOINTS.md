# Portfolio Tracker API Endpoints

## Users
- POST /api/users - Create user

## Portfolios
- POST /api/users/{userId}/portfolios - Create portfolio
- GET /api/users/{userId}/portfolios - List all portfolios for user
- GET /api/portfolios/{portfolioId} - Get single portfolio

## Transactions
- POST /api/portfolios/{portfolioId}/transactions/deposit - Deposit cash
- POST /api/portfolios/{portfolioId}/transactions/withdraw - Withdraw cash
- POST /api/portfolios/{portfolioId}/transactions/buy - Buy stock/crypto (fetches live price)
- POST /api/portfolios/{portfolioId}/transactions/sell - Sell stock/crypto (fetches live price)

## Positions
- GET /api/portfolios/{portfolioId}/positions - Get all positions
- GET /api/portfolios/{portfolioId}/positions/{symbol} - Get specific position
- GET /api/portfolios/{portfolioId}/positions/{symbol}/pnl - Get position profit/loss