export enum PluginDataType {
	String = 0,
	File = 1,
	Boolean = 2,
	Select = 3,
	Color = 4,
	Number = 5,
	TextArea = 6,
	/** Some of the options, in order: "a, b=Renamed". */
	OrderedList = 7,
	/** Replacement texts: "key=text; key2=text". */
	KeyValue = 8,
	/** Details per reference type: "type:field,field; type2:field". */
	ReferenceTypeFields = 9,
}
