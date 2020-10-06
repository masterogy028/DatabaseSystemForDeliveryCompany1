USE [projdb]
GO
/****** Object:  Trigger [dbo].[TR_TransportOffer]    Script Date: 23-Sep-20 03:42:26 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER TRIGGER [dbo].[TR_TransportOffer]
ON [dbo].[package]
AFTER INSERT
AS
BEGIN
DECLARE @type INTEGER;
SET @type = (
	SELECT i.[type] FROM inserted i
)
DECLARE @base_price INTEGER;
DECLARE @price INTEGER;
SET @base_price = (
	SELECT
		CASE
			WHEN @type = 0 THEN 115 
			WHEN @type = 1 THEN 175 
			WHEN @type = 2 THEN 250 
			WHEN @type = 3 THEN 350 
		END
)

SET @price = (
	SELECT
		CASE
			WHEN @type = 0 THEN 0 
			WHEN @type = 1 THEN 100
			WHEN @type = 2 THEN 100
			WHEN @type = 3 THEN 500 
		END
)

DECLARE @x1 INTEGER;
DECLARE @y1 INTEGER;
DECLARE @x2 INTEGER;
DECLARE @y2 INTEGER;
SET @x1 = (SELECT a.xCoordination FROM [address] a, inserted i WHERE a.idDistrict = i.[from]);
SET @y1 =(SELECT a.yCoordination FROM [address] a, inserted i WHERE a.idDistrict = i.[from]);
SET @x2 = (SELECT a.xCoordination FROM [address] a, inserted i WHERE a.idDistrict = i.[to]);
SET @y2 = (SELECT a.yCoordination FROM [address] a, inserted i WHERE a.idDistrict = i.[to]);
DECLARE @distance DECIMAL(10, 3)
SET @distance = SQRT(
	((@x1 - @x2) * (@x1 - @x2))
	+((@y1 - @y2) * (@y1 - @y2))
)

DECLARE @final_price DECIMAL(10, 3);
SET @final_price = (
	(@base_price + (SELECT i.weight FROM inserted i) * @price) * @distance
);

INSERT INTO transport_offer ([idPackage], [status], [price], [timeAccept], [timeCreate], [idDistrict])
SELECT i.idPackage, 0, @final_price, null, GETDATE(), i.[from] FROM inserted i
END
