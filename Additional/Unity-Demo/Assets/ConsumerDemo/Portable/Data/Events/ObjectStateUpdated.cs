namespace BAALLClient.Portable.Data
{
    public class ObjectStateUpdated
    {
		public ObjectStateUpdated()
        {
            Identifier = "";
			State = new State();
        }

        public string Identifier
        {
            get;
            set;
        }

		public State State{
			get;
			set;
		}
    }
}