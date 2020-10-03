package option

type Option struct {
	ShortOption string
	LongOption  string
	Description string
	HasArgs     bool
	Required    bool
	value       string
}

func New(ShortOption string, LongOption string, Description string, HasArgs bool, Required bool) Option {
	o := Option{ShortOption, LongOption, Description, HasArgs, Required, ""}
	return o
}

func (o *Option) SetValue(value string) {
	o.value = value
}

func (o *Option) Value() string {
	return o.value
}
