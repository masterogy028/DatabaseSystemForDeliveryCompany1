USE [projdb]
GO
/****** Object:  StoredProcedure [dbo].[EraseAll]    Script Date: 23-Sep-20 03:34:19 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER procedure [dbo].[EraseAll] AS
BEGIN
    SET QUOTED_IDENTIFIER ON;
    EXEC sp_MSforeachtable 'SET QUOTED_IDENTIFIER ON; ALTER TABLE ? NOCHECK CONSTRAINT ALL'
    EXEC sp_MSforeachtable 'SET QUOTED_IDENTIFIER ON; ALTER TABLE ? DISABLE TRIGGER ALL'
    EXEC sp_MSforeachtable 'SET QUOTED_IDENTIFIER ON; DELETE FROM ?'
    EXEC sp_MSforeachtable 'SET QUOTED_IDENTIFIER ON; ALTER TABLE ? CHECK CONSTRAINT ALL'
    EXEC sp_MSforeachtable 'SET QUOTED_IDENTIFIER ON; ALTER TABLE ? ENABLE TRIGGER ALL'
    EXEC sp_MSforeachtable 'SET QUOTED_IDENTIFIER ON';

    DBCC CHECKIDENT (address, RESEED , 0);
    DBCC CHECKIDENT (city, RESEED , 0);
   -- DBCC CHECKIDENT (package, RESEED , 0);
    --DBCC CHECKIDENT (transport_offers, RESEED , 0);
    --DBCC CHECKIDENT (stock, RESEED , 0);
END
