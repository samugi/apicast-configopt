package option

type Option struct {
	ShortOption string
	LongOption  string
	Description string
	HasArgs     bool
	Required    bool
	value       string
	valueB      bool
}

func New(ShortOption string, LongOption string, Description string, HasArgs bool, Required bool) Option {
	o := Option{ShortOption, LongOption, Description, HasArgs, Required, "", false}
	return o
}

func (o *Option) SetValue(value string) {
	o.value = value
}

func (o *Option) Value() string {
	return o.value
}

func (o *Option) SetValueB(value bool) {
	o.valueB = value
}

func (o *Option) ValueB() bool {
	return o.valueB
}
