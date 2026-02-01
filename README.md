# Portfolio Tracker

A multi-user portfolio management system for tracking stocks and cryptocurrencies with real-time price updates and comprehensive transaction history.

## Overview

Portfolio Tracker allows users to manage investment portfolios with support for stocks, cryptocurrencies, and cash positions. The system features role-based access control, allowing users to invite others to view or collaborate on their portfolios.

## Key Features

- **Multi-user support** with authentication and authorization
- **Portfolio management** with buy, sell, deposit, and withdraw operations
- **Automated price updates** via scheduled tasks fetching from external APIs
- **Real-time P&L calculations** based on current market prices
- **Transaction history** with complete audit trail
- **Invite system** for sharing portfolios with members or visitors
- **Role-based permissions** (Admin, Member, Visitor)

## Tech Stack

- **Backend:** Java, Spring Boot
- **Database:** MySQL
- **APIs:** External market data APIs for price updates
- **Frontend:** HTML/CSS/JavaScript (planned)

## Learning Goals

This project is being developed as a learning experience to master:
- Spring Boot framework and dependency injection
- RESTful API design
- Database design and SQL
- Scheduled tasks and background jobs
- Authentication and authorization
- External API integration

## Project Status

🚧 **In Development** - Target completion: April 30, 2026

Currently implementing core transaction processing and database architecture.

## Database Schema

The system uses 7 main tables:
- Users
- Portfolios
- Memberships
- Transactions
- Positions
- PriceQuotes
- Invites

## Authors

Kulvansh Jaswal and Kuljot Jaswal - University of Calgary Software Engineering Students
