DROP TABLE [courir_has_driven]
go

DROP TABLE [courir]
go

DROP TABLE [park]
go

DROP TABLE [vehicle]
go

DROP TABLE [stock]
go

DROP TABLE [transport_offer]
go

DROP TABLE [package]
go

DROP TABLE [user]
go

DROP TABLE [address]
go

DROP TABLE [city]
go

CREATE TABLE [address]
( 
	[idDistrict]         int  IDENTITY  NOT NULL ,
	[street]             char(100)  NULL ,
	[number]             int  NULL ,
	[xCoordination]      int  NULL ,
	[yCoordination]      int  NULL ,
	[idCity]             int  NULL 
)
go

ALTER TABLE [address]
	ADD CONSTRAINT [XPKadress] PRIMARY KEY  CLUSTERED ([idDistrict] ASC)
go

CREATE TABLE [city]
( 
	[idCity]             int  IDENTITY  NOT NULL ,
	[name]               char(100)  NULL ,
	[postalCode]         char(100)  NULL 
)
go

ALTER TABLE [city]
	ADD CONSTRAINT [XPKcity] PRIMARY KEY  CLUSTERED ([idCity] ASC)
go

ALTER TABLE [city]
	ADD CONSTRAINT [XAK1city] UNIQUE ([postalCode]  ASC)
go

CREATE TABLE [courir]
( 
	[licence]            char(100)  NULL ,
	[status]             int  NULL ,
	[profit]             decimal(10,3)  NULL ,
	[deliveriesCount]    int  NULL ,
	[licenceV]           varchar(100)  NULL ,
	[username]           varchar(100)  NOT NULL 
)
go

ALTER TABLE [courir]
	ADD CONSTRAINT [XPKcourir] PRIMARY KEY  CLUSTERED ([username] ASC)
go

CREATE TABLE [courir_has_driven]
( 
	[licenceV]           varchar(100)  NOT NULL ,
	[username]           varchar(100)  NULL 
)
go

ALTER TABLE [courir_has_driven]
	ADD CONSTRAINT [XPKcourir_has_driven] PRIMARY KEY  CLUSTERED ([licenceV] ASC)
go

CREATE TABLE [package]
( 
	[idPackage]          int  IDENTITY  NOT NULL ,
	[type]               char(18)  NULL ,
	[weight]             decimal(10,3)  NULL ,
	[to]                 int  NULL ,
	[from]               int  NULL ,
	[username]           varchar(100)  NULL 
)
go

ALTER TABLE [package]
	ADD CONSTRAINT [XPKpackage] PRIMARY KEY  CLUSTERED ([idPackage] ASC)
go

CREATE TABLE [park]
( 
	[idStock]            integer  NOT NULL ,
	[licenceV]           varchar(100)  NOT NULL 
)
go

ALTER TABLE [park]
	ADD CONSTRAINT [XPKpark] PRIMARY KEY  CLUSTERED ([idStock] ASC,[licenceV] ASC)
go

CREATE TABLE [stock]
( 
	[idStock]            integer  IDENTITY  NOT NULL ,
	[idDistrict]         int  NULL 
)
go

ALTER TABLE [stock]
	ADD CONSTRAINT [XPKstock] PRIMARY KEY  CLUSTERED ([idStock] ASC)
go

CREATE TABLE [transport_offer]
( 
	[idPackage]          int  NOT NULL ,
	[status]             int  NULL ,
	[timeAccept]         datetime  NULL ,
	[timeCreate]         datetime  NULL ,
	[price]              decimal(10,3)  NULL ,
	[idDistrict]         int  NULL 
)
go

ALTER TABLE [transport_offer]
	ADD CONSTRAINT [XPKtransport_offer] PRIMARY KEY  CLUSTERED ([idPackage] ASC)
go

CREATE TABLE [user]
( 
	[username]           varchar(100)  NOT NULL ,
	[firstname]          varchar(100)  NULL ,
	[lastname]           varchar(100)  NULL ,
	[password]           varchar(100)  NULL ,
	[isAdmin]            tinyint  NULL ,
	[idDistrict]         int  NULL 
)
go

ALTER TABLE [user]
	ADD CONSTRAINT [XPKuser] PRIMARY KEY  CLUSTERED ([username] ASC)
go

CREATE TABLE [vehicle]
( 
	[licenceV]           varchar(100)  NOT NULL ,
	[gassType]           int  NULL ,
	[gassCons]           decimal(10,3)  NULL ,
	[cap]                int  NULL 
)
go

ALTER TABLE [vehicle]
	ADD CONSTRAINT [XPKvehicle] PRIMARY KEY  CLUSTERED ([licenceV] ASC)
go


ALTER TABLE [address]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([idCity]) REFERENCES [city]([idCity])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [courir]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([licenceV]) REFERENCES [vehicle]([licenceV])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [courir]
	ADD CONSTRAINT [R_22] FOREIGN KEY ([username]) REFERENCES [user]([username])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [courir_has_driven]
	ADD CONSTRAINT [R_19] FOREIGN KEY ([licenceV]) REFERENCES [vehicle]([licenceV])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [courir_has_driven]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([username]) REFERENCES [courir]([username])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [package]
	ADD CONSTRAINT [R_14] FOREIGN KEY ([to]) REFERENCES [address]([idDistrict])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [package]
	ADD CONSTRAINT [R_15] FOREIGN KEY ([from]) REFERENCES [address]([idDistrict])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [package]
	ADD CONSTRAINT [R_25] FOREIGN KEY ([username]) REFERENCES [user]([username])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [park]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([idStock]) REFERENCES [stock]([idStock])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [park]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([licenceV]) REFERENCES [vehicle]([licenceV])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [stock]
	ADD CONSTRAINT [R_16] FOREIGN KEY ([idDistrict]) REFERENCES [address]([idDistrict])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [transport_offer]
	ADD CONSTRAINT [R_27] FOREIGN KEY ([idPackage]) REFERENCES [package]([idPackage])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [transport_offer]
	ADD CONSTRAINT [R_28] FOREIGN KEY ([idDistrict]) REFERENCES [address]([idDistrict])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [user]
	ADD CONSTRAINT [R_17] FOREIGN KEY ([idDistrict]) REFERENCES [address]([idDistrict])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go